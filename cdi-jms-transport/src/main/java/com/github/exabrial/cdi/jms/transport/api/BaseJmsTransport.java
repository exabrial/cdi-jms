package com.github.exabrial.cdi.jms.transport.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.jms.DeliveryMode;
import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
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
import com.github.exabrial.cdi.jms.transport.internal.model.JmsTransportReply;
import com.github.exabrial.cdi.jms.transport.model.exception.JmsTransportException;
import com.github.exabrial.cdi.jms.transport.model.exception.RemoteInvocationException;
import com.github.exabrial.cdi.jms.transport.model.exception.TimeoutWhileWaitingForReplyException;
import com.github.exabrial.cdi.nanoscopes.boundaryscoped.api.interceptor.Boundary;

@Boundary
abstract class BaseJmsTransport implements JmsTransport {
	static final String JMS_TRANSPORT_ERROR_REPLY = JmsTransport.class.getSimpleName() + ".isErrorReply";

	@Inject
	private MessageBodyConverter bodyConverter;
	@Inject
	private Logger log;

	@Inject
	@Config(value = "cdi-jms-transport.timeout", defaultValue = "4500")
	private Long timeout;

	@Inject
	@Config(defaultValue = "20")
	private Long additionalWaitTime;

	abstract JMSContext getJmsContext();

	@Override
	public <K extends Serializable> K sendAndWaitForReply(final Serializable payload, final Class<K> replyType)
			throws TimeoutWhileWaitingForReplyException {
		return sendAndWaitForReply(payload, replyType, timeout);
	}

	@Override
	public <K extends Serializable> K sendAndWaitForReply(final Serializable payload, final Class<K> replyType, final long replyTimeout)
			throws TimeoutWhileWaitingForReplyException {
		log.trace("sendAndWaitForReply() payload:{}", payload);
		final JMSContext jmsContext = getJmsContext();
		final Destination receiverDestination = jmsContext.createQueue(payload.getClass().getName());
		final String jmsCorrelationID = send(jmsContext, payload, replyTimeout, receiverDestination);
		final Queue replyQueue = jmsContext.createQueue(JmsTransport.class.getName());
		final String replyBody;
		try (JMSConsumer consumer = jmsContext.createConsumer(replyQueue, "JMSCorrelationID='" + jmsCorrelationID + "'")) {
			log.trace("sendAndWaitForReply() waiting:{}ms for message on consumer:{}", replyTimeout, consumer);
			final Message replyMessage = consumer.receive(replyTimeout);
			if (replyMessage == null) {
				throw new TimeoutWhileWaitingForReplyException(jmsCorrelationID, replyType);
			} else {
				replyBody = JmsMessageUtil.getBodyAsString(replyMessage);
				if (JmsMessageUtil.getBooleanProperty(JMS_TRANSPORT_ERROR_REPLY, replyMessage)) {
					throw new RemoteInvocationException(bodyConverter.fromText(JmsTransportErrorReply.class, replyBody));
				}
			}
		}
		final K reply = parseReply(replyType, replyBody);
		log.trace("sendAndWaitForReply() complete. reply:{}", reply);
		return reply;
	}

	@Override
	public <K extends Serializable> List<JmsTransportReply<K>> sendAndGatherReplies(final Serializable payload, final Class<K> replyType,
			final long waitTime) {
		log.trace("sendAndGatherReplies() payload:{} waitTime:{}", payload, waitTime);
		final JMSContext jmsContext = getJmsContext();
		final List<JmsTransportReply<K>> replies = new ArrayList<>();
		final Destination receiverDestination = jmsContext.createTopic(payload.getClass().getName());
		final String jmsCorrelationID = send(jmsContext, payload, waitTime, receiverDestination);
		final Queue replyQueue = jmsContext.createQueue(JmsTransport.class.getName());
		try (JMSConsumer consumer = jmsContext.createConsumer(replyQueue, "JMSCorrelationID='" + jmsCorrelationID + "'")) {
			final long deadline = System.currentTimeMillis() + waitTime + additionalWaitTime;
			boolean draining = true;
			while (draining) {
				final long timeRemaining = deadline - System.currentTimeMillis();
				final Message message;
				if (timeRemaining > 0) {
					message = consumer.receive(timeRemaining);
				} else {
					message = consumer.receiveNoWait();
				}
				if (message == null) {
					draining = false;
				} else {
					replies.add(toGatheredReply(replyType, message));
				}
			}
		}
		log.trace("sendAndGatherReplies() complete. replies.size():{}", replies.size());
		return replies;
	}

	protected <K extends Serializable> K parseReply(final Class<K> replyType, final String replyBody) {
		final K reply;
		if (replyBody == null) {
			reply = null;
		} else if (replyType == String.class) {
			reply = replyType.cast(replyBody);
		} else if (ClassUtils.isPrimitiveOrWrapper(replyType)) {
			reply = valueOf(replyType, replyBody);
		} else {
			reply = bodyConverter.fromText(replyType, replyBody);
		}
		return reply;
	}

	protected String send(final JMSContext jmsContext, final Serializable payload, final long timeToLive,
			final Destination receiverDestination) {
		final String jmsCorrelationID = UUID.randomUUID().toString();
		final String text = bodyConverter.toText(payload);
		final TextMessage message = jmsContext.createTextMessage(text);
		JmsMessageUtil.setJmsCorrelationID(jmsCorrelationID, message);
		JmsMessageUtil.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT, message);
		final JMSProducer producer = jmsContext.createProducer();
		JmsMessageUtil.setOgXaUuidHeader(XaUuidInterceptor.XA_UUID, XaUuidInterceptor.OG_XA_UUID, producer);
		producer.setTimeToLive(timeToLive);
		producer.setJMSCorrelationID(jmsCorrelationID);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		producer.send(receiverDestination, message);
		log.trace("send() complete. jmsCorrelationID:{}", jmsCorrelationID);
		return jmsCorrelationID;
	}

	protected <K extends Serializable> JmsTransportReply<K> toGatheredReply(final Class<K> replyType, final Message message) {
		final String replyBody = JmsMessageUtil.getBodyAsString(message);
		final JmsTransportReply<K> jmsTransportReply;
		if (JmsMessageUtil.getBooleanProperty(JMS_TRANSPORT_ERROR_REPLY, message)) {
			final JmsTransportErrorReply errorReply = bodyConverter.fromText(JmsTransportErrorReply.class, replyBody);
			jmsTransportReply = new JmsTransportReply<>(errorReply);
		} else {
			final K replyPayload = parseReply(replyType, replyBody);
			jmsTransportReply = new JmsTransportReply<>(replyPayload);
		}
		return jmsTransportReply;
	}

	static final <K extends Serializable> K valueOf(final Class<K> replyType, final String replyBody) {
		final Object parsed;
		if (replyType == Byte.class || replyType == byte.class) {
			parsed = Byte.valueOf(replyBody);
		} else if (replyType == Short.class || replyType == short.class) {
			parsed = Short.valueOf(replyBody);
		} else if (replyType == Integer.class || replyType == int.class) {
			parsed = Integer.valueOf(replyBody);
		} else if (replyType == Long.class || replyType == long.class) {
			parsed = Long.valueOf(replyBody);
		} else if (replyType == Float.class || replyType == float.class) {
			parsed = Float.valueOf(replyBody);
		} else if (replyType == Double.class || replyType == double.class) {
			parsed = Double.valueOf(replyBody);
		} else if (replyType == Boolean.class || replyType == boolean.class) {
			parsed = Boolean.valueOf(replyBody);
		} else if (replyType == Character.class || replyType == char.class) {
			parsed = replyBody.charAt(0);
		} else {
			throw new JmsTransportException("Unknown basic type:" + replyType);
		}
		return replyType.cast(parsed);
	}
}
