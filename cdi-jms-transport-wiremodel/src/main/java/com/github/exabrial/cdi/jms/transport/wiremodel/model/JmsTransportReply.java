package com.github.exabrial.cdi.jms.transport.wiremodel.model;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * One reply in a gather (scatter/gather) exchange. Wraps either a successful payload or a JmsTransportErrorReply; isErrorReply
 * distinguishes the two.
 *
 * @param <K>
 *          the successful payload type
 * @author jonathan.fisher
 */
@Getter
@EqualsAndHashCode
public class JmsTransportReply<K extends Serializable> implements Serializable {
	private static final long serialVersionUID = 1L;
	private final K payload;
	private final JmsTransportErrorReply errorReply;

	public JmsTransportReply(final K payload) {
		this.payload = payload;
		this.errorReply = null;
	}

	public JmsTransportReply(final JmsTransportErrorReply errorReply) {
		this.payload = null;
		this.errorReply = errorReply;
	}

	public boolean isErrorReply() {
		return errorReply != null;
	}

	@Override
	public String toString() {
		return "JmsTransportReply [errorReply:" + errorReply + "]";
	}
}
