package com.github.exabrial.cdi.jms.listeners.se.extension;

import jakarta.ejb.MessageDriven;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import jakarta.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Portable extension that adds @Dependent to @MessageDriven MessageListener beans so they are managed by the CDI container.
 *
 * @author jonathan.fisher
 */
public class CdiJmsListenersExtension implements Extension {
	private static final Logger log = LoggerFactory.getLogger(CdiJmsListenersExtension.class);

	<T extends MessageListener> void observeProcessAnnotatedType(
			@Observes @WithAnnotations(MessageDriven.class) final ProcessAnnotatedType<T> pat) {
		final Class<T> javaClass = pat.getAnnotatedType().getJavaClass();
		log.info("observeProcessAnnotatedType() adding @Dependent to @MessageDriven MessageListener:{}", javaClass.getName());
		pat.configureAnnotatedType().add(Dependent.Literal.INSTANCE);
	}
}
