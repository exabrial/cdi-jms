package com.github.exabrial.cdi.jms.api;

import java.io.Serializable;

import com.github.exabrial.cdi.jms.transport.wiremodel.model.DestinationParameters;

public interface JmsSender {
	void sendToQueue(Serializable object);

	void sendToQueue(Serializable object, long delay);

	void sendToQueue(Serializable object, DestinationParameters params);

	void sendToTopic(Serializable object);

	void sendToTopic(Serializable object, long delay);

	void sendToTopic(Serializable object, DestinationParameters params);
}