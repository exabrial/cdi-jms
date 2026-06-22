package com.github.exabrial.cdi.jms.listeners.se.initiator.startup;

import java.util.Set;

import jakarta.ejb.MessageDriven;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.jms.MessageListener;

import org.slf4j.Logger;

import com.github.exabrial.cdi.jms.listeners.se.api.MessageListenerManager;

@ApplicationScoped
public class CdiJmsListenersStartupInitiator {
	@Inject
	private Logger log;
	@Inject
	private MessageListenerManager managedListenerService;

	@SuppressWarnings("unchecked")
	void observeApplicationScopedInitialized(@Observes @Initialized(ApplicationScoped.class) final Object event,
			final BeanManager beanManager) {
		log.info("observeApplicationScopedInitialized() scanning for @MessageDriven MessageListener beans...");
		final Set<Bean<?>> beans = beanManager.getBeans(MessageListener.class);
		for (final Bean<?> bean : beans) {
			final Class<?> beanClass = bean.getBeanClass();
			if (beanClass.isAnnotationPresent(MessageDriven.class)) {
				log.info("observeApplicationScopedInitialized() starting beanClass:{}", beanClass.getName());
				managedListenerService.start((Class<? extends MessageListener>) beanClass);
			} else {
				log.debug("observeApplicationScopedInitialized() skipping MessageListener without @MessageDriven beanClass:{}",
						beanClass.getName());
			}
		}
		log.info("observeApplicationScopedInitialized() complete");
	}
}
