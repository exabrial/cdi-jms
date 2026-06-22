package com.github.exabrial.cdi.jms.listeners.se.model.exception;

public class CdiJmsListenersException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CdiJmsListenersException() {
		super();
	}

	public CdiJmsListenersException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CdiJmsListenersException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CdiJmsListenersException(final String message) {
		super(message);
	}

	public CdiJmsListenersException(final Throwable cause) {
		super(cause);
	}
}
