package com.github.exabrial.cdi.jms.api;

import java.io.Serializable;

/**
 * Serializes a payload object to a JMS text body and back. The default implementation uses JSON-B. Supply an @Alternative to swap in a
 * different wire format.
 *
 * @author jonathan.fisher
 */
public interface MessageBodyConverter {
	String toText(Serializable payload);

	<K extends Serializable> K fromText(Class<K> targetType, String text);
}
