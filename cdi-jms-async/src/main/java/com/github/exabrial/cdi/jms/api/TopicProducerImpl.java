package com.github.exabrial.cdi.jms.api;

import java.io.Serializable;

import jakarta.jms.JMSProducer;
import jakarta.jms.Topic;

class TopicProducerImpl implements TopicProducer {
	private final Topic topic;
	private final JMSProducer jmsProducer;
	private final MessageBodyConverter bodyConverter;

	TopicProducerImpl(final Topic topic, final JMSProducer jmsProducer, final MessageBodyConverter bodyConverter) {
		this.topic = topic;
		this.jmsProducer = jmsProducer;
		this.bodyConverter = bodyConverter;
	}

	@Override
	public void send(final Serializable object) {
		final String text = bodyConverter.toText(object);
		jmsProducer.send(topic, text);
	}
}
