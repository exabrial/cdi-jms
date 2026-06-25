package com.github.exabrial.cdi.jms.api;

import java.io.Serializable;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

import com.github.exabrial.cdi.jms.api.model.exception.MessageBodyConversionException;

/**
 * Serializes a payload object to a JMS text body and back. The default implementation uses JSON-B. Supply an @Alternative to swap in a
 * different wire format.
 *
 * @author jonathan.fisher
 */
public interface MessageBodyConverter {

	String toText(Serializable payload);

	<K extends Serializable> K fromText(Class<K> targetType, String text);

	default <K extends Serializable> K fromMessage(final Class<K> targetType, final Message message) {
		try {
			final String messageText = message.getBody(String.class);
			return fromText(targetType, messageText);
		} catch (final JMSException jmsException) {
			throw new MessageBodyConversionException(message, targetType, jmsException);
		}
	}
}
