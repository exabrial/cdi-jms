package com.github.exabrial.cdi.jms.api.model.exception;

/**
 * Raised when a low-level JMS operation invoked through JmsMessageUtil fails.
 *
 * @author jonathan.fisher
 */
public class JmsMessageException extends CdiJmsException {
	private static final long serialVersionUID = 1L;

	public JmsMessageException() {
		super();
	}

	public JmsMessageException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JmsMessageException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public JmsMessageException(final String message) {
		super(message);
	}

	public JmsMessageException(final Throwable cause) {
		super(cause);
	}
}
