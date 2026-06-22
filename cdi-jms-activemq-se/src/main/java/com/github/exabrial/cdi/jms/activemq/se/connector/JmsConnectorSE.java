package com.github.exabrial.cdi.jms.activemq.se.connector;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;

import com.github.exabrial.cdi.common.config.api.model.annotation.Config;
import com.github.exabrial.cdi.jms.internal.api.JmsConnector;

/**
 * SE alternative that produces an @ApplicationScoped ConnectionFactory from configuration.
 *
 * @author jonathan.fisher
 */
@Alternative
@Priority(1000)
@ApplicationScoped
public class JmsConnectorSE implements JmsConnector {
	@Inject
	private Logger log;

	@Inject
	@Config(value = "cdi-jms-activemq-se.broker.url", defaultValue = "failover:(tcp://127.0.0.1:61616)")
	private String brokerUrl;

	@Produces
	@ApplicationScoped
	ConnectionFactory createConnectionFactory() {
		log.info("createConnectionFactory() building ActiveMQ ConnectionFactory for brokerUrl:{}", brokerUrl);
		final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
		return activeMQConnectionFactory;
	}

	void disposeConnectionFactory(@Disposes final ConnectionFactory connectionFactory) {
		log.info("disposeConnectionFactory() disposing connectionFactory:{}", connectionFactory);
		try {
			if (connectionFactory instanceof final AutoCloseable autoCloseable) {
				autoCloseable.close();
			}
		} catch (final Exception exception) {
			log.error("disposeConnectionFactory() error closing ConnectionFactory connectionFactory:{}", connectionFactory, exception);
		}
	}
}
