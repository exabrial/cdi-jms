package com.github.exabrial.cdi.jms.api;

import java.io.Serializable;

public interface TopicProducer {
	void send(Serializable object);
}
