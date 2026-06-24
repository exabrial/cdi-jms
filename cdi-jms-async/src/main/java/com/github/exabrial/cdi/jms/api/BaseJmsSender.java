package com.github.exabrial.cdi.jms.api;

import java.io.Serializable;

import jakarta.inject.Inject;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;

import org.slf4j.Logger;

import com.github.exabrial.cdi.common.errorhandling.interceptor.XaUuidInterceptor;
import com.github.exabrial.cdi.jms.api.model.DestinationParameters;
import com.github.exabrial.cdi.jms.api.util.JmsMessageUtil;
import com.github.exabrial.cdi.nanoscopes.boundaryscoped.api.interceptor.Boundary;

@Boundary
abstract class BaseJmsSender implements JmsSender {
	@Inject
	private MessageBodyConverter bodyConverter;
	@Inject
	private Logger log;

	abstract JMSContext getJmsContext();

	@Override
	public void sendToQueue(final Serializable object) {
		log.trace("sendToQueue() object:{}", object);
		final JMSProducer producer = getJmsContext().createProducer();
		setOgXaUuidHeader(producer);
		final Queue queue = getJmsContext().createQueue(object.getClass().getName());
		final String text = bodyConverter.toText(object);
		producer.send(queue, text);
		log.trace("sendToQueue() complete");
	}

	@Override
	public void sendToQueue(final Serializable object, final long delay) {
		log.trace("sendToQueue() object:{} delay:{}", object, delay);
		final String text = bodyConverter.toText(object);
		final TextMessage message = getJmsContext().createTextMessage(text);
		JmsMessageUtil.setDelay(delay, message);
		final JMSProducer producer = getJmsContext().createProducer();
		setOgXaUuidHeader(producer);
		final Queue queue = getJmsContext().createQueue(object.getClass().getName());
		producer.send(queue, message);
		log.trace("sendToQueue() complete");
	}

	@Override
	public void sendToQueue(final Serializable object, final DestinationParameters params) {
		log.trace("sendToQueue() object:{} params:{}", object, params);
		final JMSProducer producer = getJmsContext().createProducer();
		setOgXaUuidHeader(producer);
		final String queueName = toDestinationName(object.getClass(), params);
		final Queue queue = getJmsContext().createQueue(queueName);
		final String text = bodyConverter.toText(object);
		producer.send(queue, text);
		log.trace("sendToQueue() complete");
	}

	@Override
	public void sendToTopic(final Serializable object) {
		log.trace("sendToTopic() object:{}", object);
		final JMSProducer producer = getJmsContext().createProducer();
		setOgXaUuidHeader(producer);
		final Destination topic = getJmsContext().createTopic(object.getClass().getName());
		final String text = bodyConverter.toText(object);
		producer.send(topic, text);
		log.trace("sendToTopic() complete");
	}

	@Override
	public void sendToTopic(final Serializable object, final long delay) {
		log.trace("sendToTopic() object:{} delay:{}", object, delay);
		final String text = bodyConverter.toText(object);
		final TextMessage message = getJmsContext().createTextMessage(text);
		JmsMessageUtil.setDelay(delay, message);
		final JMSProducer producer = getJmsContext().createProducer();
		setOgXaUuidHeader(producer);
		final Topic topic = getJmsContext().createTopic(object.getClass().getName());
		producer.send(topic, message);
		log.trace("sendToTopic() complete");
	}

	@Override
	public void sendToTopic(final Serializable object, final DestinationParameters params) {
		log.trace("sendToTopic() object:{} params:{}", object, params);
		final JMSProducer producer = getJmsContext().createProducer();
		setOgXaUuidHeader(producer);
		final String topicName = toDestinationName(object.getClass(), params);
		final Topic topic = getJmsContext().createTopic(topicName);
		final String text = bodyConverter.toText(object);
		producer.send(topic, text);
		log.trace("sendToTopic() complete");
	}

	static final String toDestinationName(final Class<?> objectClass, final DestinationParameters params) {
		final String destinationName;
		if (params == null || params.getDestinationExtension() == null) {
			destinationName = objectClass.getName();
		} else {
			destinationName = objectClass.getName() + "." + params.getDestinationExtension();
		}
		return destinationName;
	}

	private void setOgXaUuidHeader(final JMSProducer producer) {
		JmsMessageUtil.setOgXaUuidHeader(XaUuidInterceptor.XA_UUID, XaUuidInterceptor.OG_XA_UUID, producer);
	}
}
