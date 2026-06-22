package com.github.exabrial.cdi.jms.defaults.api.defaults;

import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;

import org.slf4j.Logger;

import com.github.exabrial.cdi.jms.api.MessageBodyConverter;
import com.github.exabrial.cdi.nanoscopes.boundaryscoped.api.interceptor.Boundary;

/**
 * Default MessageBodyConverter backed by JSON-B. Swap in a different wire format by declaring an @Alternative MessageBodyConverter and
 * enabling it in beans.xml.
 *
 * @author jonathan.fisher
 */
@Default
@ApplicationScoped
@Boundary
public class JsonbMessageBodyConverter implements MessageBodyConverter {
	@Inject
	private Logger log;
	@Inject
	private Jsonb jsonb;

	@Override
	public String toText(final Serializable payload) {
		log.trace("toText() payload:{}", payload);
		return jsonb.toJson(payload);
	}

	@Override
	public <K extends Serializable> K fromText(final Class<K> targetType, final String text) {
		log.trace("fromText() targetType:{}", targetType);
		return jsonb.fromJson(text, targetType);
	}
}
