package com.github.exabrial.cdi.jms.transport.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;

import com.github.exabrial.cdi.jms.api.qualifier.Transacted;

@Transacted
@ApplicationScoped
class TransactedJmsTransport extends BaseJmsTransport {
	@Inject
	@Transacted
	private JMSContext jmsContext;

	@Override
	JMSContext getJmsContext() {
		return jmsContext;
	}
}
