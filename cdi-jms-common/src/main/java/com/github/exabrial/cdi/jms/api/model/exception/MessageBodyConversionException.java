package com.github.exabrial.cdi.jms.api.model.exception;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

public class MessageBodyConversionException extends CdiJmsException {
	private static final long serialVersionUID = 1L;

	public MessageBodyConversionException(final Message message, final Class<?> targetType, final JMSException jmse) {
		// TODO Auto-generated constructor stub
	}
}
