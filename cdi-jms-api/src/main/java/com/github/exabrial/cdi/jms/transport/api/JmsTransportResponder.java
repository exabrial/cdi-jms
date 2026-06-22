package com.github.exabrial.cdi.jms.transport.api;

import java.io.Serializable;

import jakarta.jms.Message;

public interface JmsTransportResponder {
	void reply(Message originalMessage, Serializable replyPayload);
}
