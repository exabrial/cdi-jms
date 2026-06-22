package com.github.exabrial.cdi.jms.listeners.se.model;

public interface ActivationConfigProperties {
	String DESTINATION = "destination";
	String DESTINATION_TYPE = "destinationType";
	String MESSAGE_SELECTOR = "messageSelector";
	String MAX_SESSIONS = "maxSessions";

	String DESTINATION_TYPE_QUEUE = "jakarta.jms.Queue";
	String DESTINATION_TYPE_TOPIC = "jakarta.jms.Topic";
}
