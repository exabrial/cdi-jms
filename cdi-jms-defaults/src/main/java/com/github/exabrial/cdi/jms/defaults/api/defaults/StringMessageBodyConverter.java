package com.github.exabrial.cdi.jms.defaults.api.defaults;

import java.io.Serializable;
import java.lang.reflect.Constructor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import com.github.exabrial.cdi.jms.api.MessageBodyConverter;
import com.github.exabrial.cdi.jms.transport.wiremodel.model.exception.JmsTransportException;

/**
 * Alternative MessageBodyConverter that uses toString() for serialization and a String constructor for deserialization. Enable in
 * beans.xml to use when JSON-B is not on the classpath.
 *
 * @author jonathan.fisher
 */
@Alternative
@ApplicationScoped
public class StringMessageBodyConverter implements MessageBodyConverter {
	@Inject
	private Logger log;

	@Override
	public String toText(final Serializable payload) {
		log.trace("toText() payload:{}", payload);
		final String text;
		if (payload == null) {
			text = null;
		} else {
			text = payload.toString();
		}
		return text;
	}

	@Override
	public <K extends Serializable> K fromText(final Class<K> targetType, final String text) {
		log.trace("fromText() targetType:{}", targetType);
		final K result;
		if (text == null) {
			result = null;
		} else if (targetType == String.class) {
			result = targetType.cast(text);
		} else {
			result = constructFromString(targetType, text);
		}
		return result;
	}

	static final <K extends Serializable> K constructFromString(final Class<K> targetType, final String text) {
		try {
			final Constructor<K> constructor = targetType.getConstructor(String.class);
			return constructor.newInstance(text);
		} catch (final NoSuchMethodException noSuchMethodException) {
			throw new JmsTransportException("No String constructor found on targetType:" + targetType.getName(), noSuchMethodException);
		} catch (final Exception exception) {
			throw new JmsTransportException("Failed to construct targetType:" + targetType.getName() + " from text", exception);
		}
	}
}
