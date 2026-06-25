package com.github.exabrial.cdi.jms.api;

import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.Topic;

import org.slf4j.Logger;

abstract class BaseTopicProducerFactory implements TopicProducerFactory {
	@Inject
	private Logger log;
	@Inject
	private MessageBodyConverter bodyConverter;

	abstract JMSContext getJmsContext();

	@Override
	public TopicProducer createTopicProducer(final Class<?> messageType, final String topicExtension) {
		final String topicName = messageType.getName() + "." + topicExtension;
		log.info("createTopicProducer() topicName:{}", topicName);
		final Topic topic = getJmsContext().createTopic(topicName);
		final JMSProducer jmsProducer = getJmsContext().createProducer();
		return new TopicProducerImpl(topic, jmsProducer, bodyConverter);
	}
}
