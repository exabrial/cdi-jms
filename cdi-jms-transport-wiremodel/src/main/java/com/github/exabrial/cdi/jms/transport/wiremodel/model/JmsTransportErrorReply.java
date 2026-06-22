package com.github.exabrial.cdi.jms.transport.wiremodel.model;

import java.io.Serializable;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries a remote exception back to the caller of a request/reply exchange. The serving side serializes one of these in place of a
 * normal reply, flags the message with the transport's error-reply property, and the originating side reconstructs it and raises a
 * RemoteInvocationException.
 *
 * @author jonathan.fisher
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class JmsTransportErrorReply implements Serializable {
	private static final long serialVersionUID = 1L;

	private String exceptionClassName;
	private String message;
	private String stackTrace;

	public JmsTransportErrorReply(final Exception exception) {
		this.exceptionClassName = exception.getClass().getName();
		this.message = exception.getMessage();
		this.stackTrace = ExceptionUtils.getStackTrace(exception);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("\nRemote Exception:");
		sb.append(exceptionClassName);
		sb.append("\nRemote Message:");
		sb.append(message);
		sb.append("\nRemote Stack trace:\n");
		sb.append(stackTrace);
		return sb.toString();
	}
}
