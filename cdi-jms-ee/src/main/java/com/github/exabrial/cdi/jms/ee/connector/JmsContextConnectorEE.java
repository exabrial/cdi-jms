package com.github.exabrial.cdi.jms.ee.connector;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;

import org.slf4j.Logger;

import com.github.exabrial.cdi.common.api.qualifier.NonTransacted;
import com.github.exabrial.cdi.common.api.qualifier.Transacted;
import com.github.exabrial.cdi.jms.internal.api.JmsContextConnector;

/**
 * Default EE connector. Injects JMSContext from JNDI connection factories and produces them with @Transacted/@NonTransacted
 * qualifiers.
 *
 * @author jonathan.fisher
 */
@Default
@ApplicationScoped
public class JmsContextConnectorEE implements JmsContextConnector {
	@Inject
	private Logger log;

	@Inject
	@JMSConnectionFactory("jms/xaConnectionFactory")
	private JMSContext xaJmsContext;

	@Inject
	@JMSConnectionFactory("jms/connectionFactory")
	private JMSContext nonXaJmsContext;

	@Produces
	@Transacted
	JMSContext createTransactedJmsContext() {
		log.trace("createTransactedJmsContext() producing @Transacted JMSContext");
		return xaJmsContext;
	}

	@Produces
	@NonTransacted
	JMSContext createNonTransactedJmsContext() {
		log.trace("createNonTransactedJmsContext() producing @NonTransacted JMSContext");
		return nonXaJmsContext;
	}
}
