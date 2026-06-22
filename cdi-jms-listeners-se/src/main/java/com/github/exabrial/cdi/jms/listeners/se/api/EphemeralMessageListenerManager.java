package com.github.exabrial.cdi.jms.listeners.se.api;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import com.github.exabrial.cdi.jms.listeners.se.model.ListenerHandle;
import com.github.exabrial.cdi.jms.listeners.se.model.ListenerState;
import com.github.exabrial.cdi.jms.transport.wiremodel.model.DestinationParameters;

/**
 * Manages ephemeral MessageListener instances with caller-specified destinations. All operations are by handle.
 *
 * @author jonathan.fisher
 */
@ApplicationScoped
public class EphemeralMessageListenerManager {
	@Inject
	private Logger log;
	@Inject
	private BeanManager beanManager;
	@Inject
	private ConnectionFactory connectionFactory;

	private final Map<ListenerHandle, ListenerContext> handles = new ConcurrentHashMap<>();
	private volatile long counter = 0;

	public ListenerHandle start(final Class<? extends MessageListener> messageListenerClazz,
			final Class<? extends Serializable> destination, final Class<? extends Destination> destinationType,
			final DestinationParameters params) {
		log.info("start() starting managed ephemeral messageListenerClazz:{} destination:{}", messageListenerClazz.getName(),
				destination.getName());
		final Bean<?> bean = beanManager.resolve(beanManager.getBeans(messageListenerClazz));
		final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
		final MessageListener messageListener = (MessageListener) beanManager.getReference(bean, messageListenerClazz, creationalContext);

		final ListenerHandle listenerHandle = startInternal(messageListener, destination, destinationType, params, creationalContext);
		return listenerHandle;
	}

	public ListenerHandle start(final MessageListener messageListener, final Class<? extends Serializable> destination,
			final Class<? extends Destination> destinationType, final DestinationParameters params) {
		log.info("start() starting unmanaged ephemeral messageListener:{} destination:{}", messageListener.getClass().getName(),
				destination.getName());
		final ListenerHandle listenerHandle = startInternal(messageListener, destination, destinationType, params, null);
		return listenerHandle;
	}

	public void stop(final ListenerHandle handle) {
		log.info("stop() stopping handle:{}", handle);
		final ListenerContext listenerContext = handles.remove(handle);
		if (listenerContext == null) {
			log.warn("stop() handle not found, ignoring handle:{}", handle);
		} else {
			try {
				listenerContext.getJmsContext().close();
			} catch (final Exception exception) {
				log.error("stop() error closing JMSContext for handle:{}", handle, exception);
			}
			if (listenerContext.getCreationalContext() != null) {
				try {
					listenerContext.getCreationalContext().release();
				} catch (final Exception exception) {
					log.error("stop() error releasing CreationalContext for handle:{}", handle, exception);
				}
			}
			log.info("stop() stopped handle:{}", handle);
		}
	}

	public ListenerState getState(final ListenerHandle handle) {
		log.debug("getState() handle:{}", handle);
		final ListenerContext listenerContext = handles.get(handle);
		final ListenerState listenerState;
		if (listenerContext == null) {
			listenerState = null;
		} else {
			listenerState = listenerContext.getListenerState();
		}
		return listenerState;
	}

	public void pause(final ListenerHandle handle) {
		log.info("pause() pausing handle:{}", handle);
		final ListenerContext listenerContext = handles.get(handle);
		if (listenerContext == null) {
			log.warn("pause() handle not found, ignoring handle:{}", handle);
		} else {
			listenerContext.getJmsContext().stop();
			listenerContext.setListenerState(ListenerState.PAUSED);
			log.info("pause() paused handle:{}", handle);
		}
	}

	public void resume(final ListenerHandle handle) {
		log.info("resume() resuming handle:{}", handle);
		final ListenerContext listenerContext = handles.get(handle);
		if (listenerContext == null) {
			log.warn("resume() handle not found, ignoring handle:{}", handle);
		} else {
			listenerContext.getJmsContext().start();
			listenerContext.setListenerState(ListenerState.RUNNING);
			log.info("resume() resumed handle:{}", handle);
		}
	}

	private ListenerHandle startInternal(final MessageListener messageListener, final Class<? extends Serializable> destination,
			final Class<? extends Destination> destinationType, final DestinationParameters params,
			final CreationalContext<?> creationalContext) {
		final String destinationName = toDestinationName(destination, params);
		final String selector = toSelector(params);

		final JMSContext jmsContext = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);
		final Destination jmsDestination = MessageListenerManager.lookupDestination(destinationName, destinationType.getName(),
				messageListener.getClass(), jmsContext);
		final JMSConsumer jmsConsumer = jmsContext.createConsumer(jmsDestination, selector);
		jmsConsumer.setMessageListener(messageListener);
		jmsContext.start();

		final ListenerHandle listenerHandle = new ListenerHandle(messageListener.getClass(), jmsDestination, (int) counter++);
		final ListenerContext listenerContext = new ListenerContext(jmsContext, creationalContext);
		listenerContext.setListenerState(ListenerState.RUNNING);
		handles.put(listenerHandle, listenerContext);
		log.info("startInternal() started listenerHandle:{}", listenerHandle);
		return listenerHandle;
	}

	static final String toDestinationName(final Class<? extends Serializable> destination, final DestinationParameters params) {
		final String destinationName;
		if (params == null || params.getDestinationExtension() == null) {
			destinationName = destination.getName();
		} else {
			destinationName = destination.getName() + "." + params.getDestinationExtension();
		}
		return destinationName;
	}

	static final String toSelector(final DestinationParameters params) {
		final String selector;
		if (params == null) {
			selector = null;
		} else {
			selector = params.getSelector();
		}
		return selector;
	}
}
