package com.github.exabrial.cdi.jms.transport.api;

import java.io.Serializable;
import java.util.Objects;

import jakarta.inject.Inject;
import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;

import com.github.exabrial.cdi.common.config.api.model.annotation.Config;
import com.github.exabrial.cdi.common.errorhandling.interceptor.XaUuidInterceptor;
import com.github.exabrial.cdi.jms.api.MessageBodyConverter;
import com.github.exabrial.cdi.jms.api.util.JmsMessageUtil;
import com.github.exabrial.cdi.jms.transport.internal.model.JmsTransportErrorReply;
import com.github.exabrial.cdi.nanoscopes.boundaryscoped.api.interceptor.Boundary;

@Boundary
abstract class BaseJmsTransportResponder implements JmsTransportResponder {
	@Inject
	private MessageBodyConverter bodyConverter;
	@Inject
	private Logger log;

	@Inject
	@Config(value = "cdi-jms-transport.timeout", defaultValue = "4500")
	private Long timeout;

	abstract JMSContext getJmsContext();

	@Override
	public void reply(final Message originalMessage, final Serializable replyPayload) {
		final String jmsCorrelationID = JmsMessageUtil.getJmsCorrelationID(originalMessage);
		log.trace("reply() jmsCorrelationID:{} replyPayload:{}", jmsCorrelationID, replyPayload);

		final String text = toText(replyPayload);
		final boolean isErrorReply = replyPayload instanceof JmsTransportErrorReply;
		final JMSContext jmsContext = getJmsContext();

		final TextMessage message = jmsContext.createTextMessage(text);
		JmsMessageUtil.setJmsCorrelationID(jmsCorrelationID, message);
		JmsMessageUtil.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT, message);
		JmsMessageUtil.setBooleanProperty(BaseJmsTransport.JMS_TRANSPORT_ERROR_REPLY, isErrorReply, message);

		final JMSProducer producer = jmsContext.createProducer();
		JmsMessageUtil.setOgXaUuidHeader(XaUuidInterceptor.XA_UUID, XaUuidInterceptor.OG_XA_UUID, producer);
		producer.setTimeToLive(timeout);
		producer.setJMSCorrelationID(jmsCorrelationID);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		final Queue replyQueue = jmsContext.createQueue(JmsTransport.class.getName());
		producer.send(replyQueue, message);
		log.trace("reply() complete");
	}

	private String toText(final Serializable replyPayload) {
		final String text;
		if (replyPayload == null) {
			text = null;
		} else if (replyPayload instanceof final String stringPayload) {
			text = stringPayload;
		} else if (ClassUtils.isPrimitiveOrWrapper(replyPayload.getClass())) {
			text = Objects.toString(replyPayload);
		} else {
			text = bodyConverter.toText(replyPayload);
		}
		return text;
	}
}
