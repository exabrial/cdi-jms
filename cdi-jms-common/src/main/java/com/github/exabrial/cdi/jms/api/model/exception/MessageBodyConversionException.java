package com.github.exabrial.cdi.jms.api.model.exception;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

/**
 * Raised when a JMS message body cannot be converted to the requested payload type.
 *
 * @author jonathan.fisher
 */
public class MessageBodyConversionException extends CdiJmsException {
	private static final long serialVersionUID = 1L;

	public MessageBodyConversionException(final Message message, final Class<?> targetType, final JMSException jmsException) {
		super("failed to convert message body to targetType:" + targetType.getName() + " message:" + message, jmsException);
	}
}
