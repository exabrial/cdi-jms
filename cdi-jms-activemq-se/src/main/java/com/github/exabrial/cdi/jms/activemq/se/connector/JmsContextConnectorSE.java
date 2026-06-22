package com.github.exabrial.cdi.jms.activemq.se.connector;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;

import org.slf4j.Logger;

import com.github.exabrial.cdi.jms.api.qualifier.NonTransacted;
import com.github.exabrial.cdi.jms.api.qualifier.Transacted;
import com.github.exabrial.cdi.jms.internal.api.JmsContextConnector;
import com.github.exabrial.cdi.jms.transport.wiremodel.model.exception.JmsTransportException;
import com.github.exabrial.cdi.nanoscopes.boundaryscoped.api.scope.BoundaryScoped;

/**
 * SE alternative that overrides the EE context connector. The @NonTransacted context is real and @BoundaryScoped. The @Transacted
 * context throws on use â no JTA in SE.
 *
 * @author jonathan.fisher
 */
@Alternative
@Priority(1000)
@ApplicationScoped
public class JmsContextConnectorSE implements JmsContextConnector {
	@Inject
	private Logger log;
	@Inject
	private ConnectionFactory connectionFactory;

	@Produces
	@NonTransacted
	@BoundaryScoped
	JMSContext createNonTransactedJmsContext() {
		log.trace("createNonTransactedJmsContext() opening non-transactional JMSContext");
		return connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);
	}

	void disposeNonTransactedJmsContext(@Disposes @NonTransacted final JMSContext jmsContext) {
		log.trace("disposeNonTransactedJmsContext() closing JMSContext jmsContext:{}", jmsContext);
		try {
			jmsContext.close();
		} catch (final Exception exception) {
			log.error("disposeNonTransactedJmsContext() error closing JMSContext jmsContext:{}", jmsContext, exception);
		}
	}

	@Produces
	@Transacted
	@BoundaryScoped
	JMSContext createTransactedJmsContext() {
		throw new JmsTransportException("@Transacted JMSContext is not available in a CDI SE container. There is no JTA.");
	}
}
