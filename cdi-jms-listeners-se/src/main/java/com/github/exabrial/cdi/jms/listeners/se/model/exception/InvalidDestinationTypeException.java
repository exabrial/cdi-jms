package com.github.exabrial.cdi.jms.listeners.se.model.exception;

import jakarta.jms.MessageListener;

public class InvalidDestinationTypeException extends CdiJmsListenersException {
	private static final long serialVersionUID = 1L;

	public InvalidDestinationTypeException(final String destinationType, final Class<? extends MessageListener> messageListenerClazz) {
		super("Unexpected destination type:" + destinationType + " on MessageListener:" + messageListenerClazz.getName());
	}
}
