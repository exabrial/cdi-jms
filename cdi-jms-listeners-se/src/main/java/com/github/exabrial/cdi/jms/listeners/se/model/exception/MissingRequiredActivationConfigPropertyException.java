package com.github.exabrial.cdi.jms.listeners.se.model.exception;

import jakarta.jms.MessageListener;

public class MissingRequiredActivationConfigPropertyException extends CdiJmsListenersException {
	private static final long serialVersionUID = 1L;

	public MissingRequiredActivationConfigPropertyException(final String missingPropertyName,
			final Class<? extends MessageListener> onMessageListenerClazz) {
		super("Missing required activationConfig property:" + missingPropertyName + " on MessageListener:"
				+ onMessageListenerClazz.getName());
	}
}
