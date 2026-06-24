package com.github.exabrial.cdi.jms.transport.model.exception;

import com.github.exabrial.cdi.jms.transport.internal.model.JmsTransportErrorReply;

/**
 * Raised on the originating side when a request/reply exchange comes back carrying a remote exception rather than a normal reply.
 *
 * @author jonathan.fisher
 */
public class RemoteInvocationException extends JmsTransportException {
	private static final long serialVersionUID = 1L;
	private final String remoteExceptionMessage;

	public RemoteInvocationException(final JmsTransportErrorReply actualReply) {
		super(actualReply.toString());
		remoteExceptionMessage = actualReply.getMessage();
	}

	public String getRemoteExceptionMessage() {
		return remoteExceptionMessage;
	}
}
