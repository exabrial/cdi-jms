package com.github.exabrial.cdi.jms.listeners.se.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.annotation.PreDestroy;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.MessageListener;

import org.slf4j.Logger;

import com.github.exabrial.cdi.jms.listeners.se.internal.model.ListenerContext;
import com.github.exabrial.cdi.jms.listeners.se.model.ActivationConfigProperties;
import com.github.exabrial.cdi.jms.listeners.se.model.ListenerHandle;
import com.github.exabrial.cdi.jms.listeners.se.model.ListenerState;
import com.github.exabrial.cdi.jms.listeners.se.model.exception.InvalidDestinationTypeException;
import com.github.exabrial.cdi.jms.listeners.se.model.exception.ListenerAlreadyStartedException;
import com.github.exabrial.cdi.jms.listeners.se.model.exception.MissingRequiredActivationConfigPropertyException;

/**
 * Manages @MessageDriven MessageListener beans discovered by the CDI container. All operations are by listener class.
 *
 * @author jonathan.fisher
 */
@ApplicationScoped
public class MessageListenerManager {
	@Inject
	private Logger log;
	@Inject
	private BeanManager beanManager;
	@Inject
	private ConnectionFactory connectionFactory;

	private final Map<ListenerHandle, ListenerContext> handles = new ConcurrentHashMap<>();
	private volatile long counter = 0;

	@PreDestroy
	void destroy() {
		log.info("destroy() stopping all managed listeners handleCount:{}", handles.size());
		for (final ListenerHandle handle : new ArrayList<>(handles.keySet())) {
			final ListenerContext listenerContext = handles.remove(handle);
			try {
				listenerContext.getJmsContext().close();
			} catch (final Exception exception) {
				log.error("destroy() error closing JMSContext for handle:{}", handle, exception);
			}
			try {
				listenerContext.getCreationalContext().release();
			} catch (final Exception exception) {
				log.error("destroy() error releasing CreationalContext for handle:{}", handle, exception);
			}
		}
	}

	public void start(final Class<? extends MessageListener> messageListenerClazz) {
		log.info("start() starting messageListenerClazz:{}", messageListenerClazz.getName());

		final boolean alreadyRunning = alreadyRunning(messageListenerClazz);
		if (alreadyRunning) {
			throw new ListenerAlreadyStartedException(messageListenerClazz);
		} else {
			final MessageDriven messageDrivenConfiguration = messageListenerClazz.getAnnotation(MessageDriven.class);
			final List<ActivationConfigProperty> activationConfig = Arrays.asList(messageDrivenConfiguration.activationConfig());

			final String destinationName = toDestinationName(messageListenerClazz, activationConfig);
			final String destinationType = toDestinationType(messageListenerClazz, activationConfig);
			final String selector = toSelector(messageListenerClazz, activationConfig);
			final int maxSessions = toMaxSessions(activationConfig);

			for (int sessionIndex = 0; sessionIndex < maxSessions; sessionIndex++) {
				final JMSContext jmsContext = connectionFactory.createContext(null, null, JMSContext.AUTO_ACKNOWLEDGE);
				final Destination destination = lookupDestination(destinationName, destinationType, messageListenerClazz, jmsContext);
				final JMSConsumer jmsConsumer = jmsContext.createConsumer(destination, selector);

				final Bean<?> bean = beanManager.resolve(beanManager.getBeans(messageListenerClazz));
				final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
				final MessageListener messageListener = (MessageListener) beanManager.getReference(bean, messageListenerClazz,
						creationalContext);
				jmsConsumer.setMessageListener(messageListener);
				jmsContext.start();

				final ListenerHandle listenerHandle = new ListenerHandle(messageListenerClazz, destination, (int) counter++);
				final ListenerContext listenerContext = new ListenerContext(jmsContext, creationalContext);
				listenerContext.setListenerState(ListenerState.RUNNING);
				handles.put(listenerHandle, listenerContext);
				log.info("start() started listenerHandle:{}", listenerHandle);
			}
		}
	}

	public void stop(final Class<? extends MessageListener> messageListenerClazz) {
		log.info("stop() stopping all instances of messageListenerClazz:{}", messageListenerClazz.getName());
		final Set<ListenerHandle> matchingHandles = matchingHandles(messageListenerClazz);
		for (final ListenerHandle handle : matchingHandles) {
			final ListenerContext listenerContext = handles.remove(handle);
			try {
				listenerContext.getJmsContext().close();
			} catch (final Exception exception) {
				log.error("stop() error closing JMSContext for handle:{}", handle, exception);
			}
			try {
				listenerContext.getCreationalContext().release();
			} catch (final Exception exception) {
				log.error("stop() error releasing CreationalContext for handle:{}", handle, exception);
			}
			log.info("stop() stopped handle:{}", handle);
		}
	}

	public ListenerState getState(final Class<? extends MessageListener> messageListenerClazz) {
		log.debug("getState() messageListenerClazz:{}", messageListenerClazz.getName());
		final ListenerState listenerState = matchingHandles(messageListenerClazz).stream()
				.map((final ListenerHandle handle) -> handles.get(handle))
				.filter((final ListenerContext listenerContext) -> listenerContext != null).map(ListenerContext::getListenerState).findFirst()
				.orElse(null);
		return listenerState;
	}

	public void pause(final Class<? extends MessageListener> messageListenerClazz) {
		log.info("pause() pausing messageListenerClazz:{}", messageListenerClazz.getName());
		final Set<ListenerHandle> matchingHandles = matchingHandles(messageListenerClazz);
		for (final ListenerHandle handle : matchingHandles) {
			final ListenerContext listenerContext = handles.get(handle);
			if (listenerContext == null) {
				log.warn("pause() handle not found, ignoring handle:{}", handle);
			} else {
				listenerContext.getJmsContext().stop();
				listenerContext.setListenerState(ListenerState.PAUSED);
				log.info("pause() paused handle:{}", handle);
			}
		}
	}

	public void resume(final Class<? extends MessageListener> messageListenerClazz) {
		log.info("resume() resuming messageListenerClazz:{}", messageListenerClazz.getName());
		final Set<ListenerHandle> matchingHandles = matchingHandles(messageListenerClazz);
		for (final ListenerHandle handle : matchingHandles) {
			final ListenerContext listenerContext = handles.get(handle);
			if (listenerContext == null) {
				log.warn("resume() handle not found, ignoring handle:{}", handle);
			} else {
				listenerContext.getJmsContext().start();
				listenerContext.setListenerState(ListenerState.RUNNING);
				log.info("resume() resumed handle:{}", handle);
			}
		}
	}

	protected boolean alreadyRunning(final Class<? extends MessageListener> messageListenerClazz) {
		return handles.keySet().stream()
				.anyMatch((final ListenerHandle handle) -> messageListenerClazz.equals(handle.getMessageListenerClazz()));
	}

	protected Set<ListenerHandle> matchingHandles(final Class<? extends MessageListener> messageListenerClazz) {
		return handles.keySet().stream()
				.filter((final ListenerHandle handle) -> messageListenerClazz.equals(handle.getMessageListenerClazz()))
				.collect(Collectors.toSet());
	}

	static final Destination lookupDestination(final String destinationName, final String destinationType,
			final Class<? extends MessageListener> messageListenerClazz, final JMSContext jmsContext) {
		final Destination destination = switch (destinationType) {
			case ActivationConfigProperties.DESTINATION_TYPE_TOPIC -> jmsContext.createTopic(destinationName);
			case ActivationConfigProperties.DESTINATION_TYPE_QUEUE -> jmsContext.createQueue(destinationName);
			default -> throw new InvalidDestinationTypeException(destinationType, messageListenerClazz);
		};
		return destination;
	}

	static final String toDestinationName(final Class<? extends MessageListener> messageListenerClazz,
			final List<ActivationConfigProperty> activationConfig) {
		final String destinationName = activationConfig.stream()
				.filter((final ActivationConfigProperty candidate) -> ActivationConfigProperties.DESTINATION.equals(candidate.propertyName()))
				.map(ActivationConfigProperty::propertyValue).findFirst().orElseThrow(
						() -> new MissingRequiredActivationConfigPropertyException(ActivationConfigProperties.DESTINATION, messageListenerClazz));
		return destinationName;
	}

	static final String toDestinationType(final Class<? extends MessageListener> messageListenerClazz,
			final List<ActivationConfigProperty> activationConfig) {
		final String destinationType = activationConfig.stream()
				.filter(
						(final ActivationConfigProperty candidate) -> ActivationConfigProperties.DESTINATION_TYPE.equals(candidate.propertyName()))
				.map(ActivationConfigProperty::propertyValue).findFirst()
				.orElseThrow(() -> new MissingRequiredActivationConfigPropertyException(ActivationConfigProperties.DESTINATION_TYPE,
						messageListenerClazz));
		return destinationType;
	}

	static final int toMaxSessions(final List<ActivationConfigProperty> activationConfig) {
		final int maxSessions = activationConfig.stream()
				.filter((final ActivationConfigProperty candidate) -> ActivationConfigProperties.MAX_SESSIONS.equals(candidate.propertyName()))
				.map(ActivationConfigProperty::propertyValue).findFirst().map(Integer::parseInt).orElse(1);
		return maxSessions;
	}

	static final String toSelector(final Class<? extends MessageListener> messageListenerClazz,
			final List<ActivationConfigProperty> activationConfig) {
		final String selector = activationConfig.stream()
				.filter(
						(final ActivationConfigProperty candidate) -> ActivationConfigProperties.MESSAGE_SELECTOR.equals(candidate.propertyName()))
				.map(ActivationConfigProperty::propertyValue).findFirst().orElse(null);
		return selector;
	}
}
