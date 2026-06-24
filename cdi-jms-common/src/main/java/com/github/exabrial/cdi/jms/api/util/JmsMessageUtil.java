package com.github.exabrial.cdi.jms.api.util;

import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.Message;

import org.slf4j.MDC;

import com.github.exabrial.cdi.jms.api.model.exception.JmsMessageException;

/**
 * Static JMS message utility methods shared across sender, transport, and responder.
 *
 * @author jonathan.fisher
 */
public final class JmsMessageUtil {
	private static final String AMQ_SCHEDULED_DELAY = "AMQ_SCHEDULED_DELAY";

	private JmsMessageUtil() {
	}

	public static boolean getBooleanProperty(final String propertyName, final Message message) {
		try {
			return message.getBooleanProperty(propertyName);
		} catch (final JMSException jmsException) {
			throw new JmsMessageException(jmsException);
		}
	}

	public static String getBodyAsString(final Message message) {
		try {
			return message.getBody(String.class);
		} catch (final JMSException jmsException) {
			throw new JmsMessageException(jmsException);
		}
	}

	public static String getJmsCorrelationID(final Message message) {
		try {
			return message.getJMSCorrelationID();
		} catch (final JMSException jmsException) {
			throw new JmsMessageException(jmsException);
		}
	}

	public static void setBooleanProperty(final String propertyName, final boolean value, final Message message) {
		try {
			message.setBooleanProperty(propertyName, value);
		} catch (final JMSException jmsException) {
			throw new JmsMessageException(jmsException);
		}
	}

	public static void setDelay(final long delay, final Message message) {
		try {
			message.setLongProperty(AMQ_SCHEDULED_DELAY, delay);
		} catch (final JMSException jmsException) {
			throw new JmsMessageException(jmsException);
		}
	}

	public static void setJMSDeliveryMode(final int deliveryMode, final Message message) {
		try {
			message.setJMSDeliveryMode(deliveryMode);
		} catch (final JMSException jmsException) {
			throw new JmsMessageException(jmsException);
		}
	}

	public static void setJmsCorrelationID(final String jmsCorrelationID, final Message message) {
		try {
			message.setJMSCorrelationID(jmsCorrelationID);
		} catch (final JMSException jmsException) {
			throw new JmsMessageException(jmsException);
		}
	}

	public static void setOgXaUuidHeader(final String xaUuidKey, final String ogXaUuidKey, final JMSProducer producer) {
		final String ogXaUuid = MDC.get(xaUuidKey);
		if (ogXaUuid != null) {
			producer.setProperty(ogXaUuidKey, ogXaUuid);
		}
	}
}
