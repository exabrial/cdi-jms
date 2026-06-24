package com.github.exabrial.cdi.jms.transport.model.exception;

import com.github.exabrial.cdi.jms.api.model.exception.CdiJmsException;

/**
 * Base type for all failures originating in the JMS transport.
 *
 * @author jonathan.fisher
 */
public class JmsTransportException extends CdiJmsException {
	private static final long serialVersionUID = 1L;

	public JmsTransportException() {
		super();
	}

	public JmsTransportException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JmsTransportException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public JmsTransportException(final String message) {
		super(message);
	}

	public JmsTransportException(final Throwable cause) {
		super(cause);
	}
}
