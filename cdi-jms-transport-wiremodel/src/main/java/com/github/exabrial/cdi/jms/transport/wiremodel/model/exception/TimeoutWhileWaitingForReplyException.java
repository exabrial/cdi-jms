package com.github.exabrial.cdi.jms.transport.wiremodel.model.exception;

/**
 * Thrown by sendAndWaitForReply when no reply arrives within the timeout window.
 *
 * @author jonathan.fisher
 */
public class TimeoutWhileWaitingForReplyException extends JmsTransportException {
	private static final long serialVersionUID = 1L;

	public TimeoutWhileWaitingForReplyException(final String jmsCorrelationID, final Class<?> replyType) {
		super("TimeoutWhileWaitingForReplyException jmsCorrelationID:" + jmsCorrelationID + " replyType:" + replyType.getName());
	}
}
