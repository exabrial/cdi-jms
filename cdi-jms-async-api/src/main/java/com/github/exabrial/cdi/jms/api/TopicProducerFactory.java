package com.github.exabrial.cdi.jms.api;

public interface TopicProducerFactory {
	TopicProducer createTopicProducer(Class<?> messageType, String topicExtension);
}
