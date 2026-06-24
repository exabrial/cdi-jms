package com.github.exabrial.cdi.jms.api.model.exception;

/**
 * Base type for all failures originating in the JMS transport.
 *
 * @author jonathan.fisher
 */
public class CdiJmsException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CdiJmsException() {
		super();
	}

	public CdiJmsException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CdiJmsException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CdiJmsException(final String message) {
		super(message);
	}

	public CdiJmsException(final Throwable cause) {
		super(cause);
	}
}
