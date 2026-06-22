package com.github.exabrial.cdi.jms.listeners.se.model.exception;

import jakarta.jms.MessageListener;

public class ListenerAlreadyStartedException extends CdiJmsListenersException {
	private static final long serialVersionUID = 1L;

	public ListenerAlreadyStartedException(final Class<? extends MessageListener> messageListenerClazz) {
		super("Listener already started for messageListenerClazz:" + messageListenerClazz.getName());
	}
}
