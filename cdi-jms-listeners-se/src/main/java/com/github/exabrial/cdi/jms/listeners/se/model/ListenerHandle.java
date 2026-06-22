package com.github.exabrial.cdi.jms.listeners.se.model;

import jakarta.jms.Destination;
import jakarta.jms.MessageListener;

import lombok.Data;

@Data
public class ListenerHandle {
	private final Class<? extends MessageListener> messageListenerClazz;
	private final Destination destination;
	private final int sequence;
}
