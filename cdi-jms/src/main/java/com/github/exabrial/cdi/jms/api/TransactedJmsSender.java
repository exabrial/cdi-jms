package com.github.exabrial.cdi.jms.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;

import com.github.exabrial.cdi.jms.api.qualifier.Transacted;

@Transacted
@ApplicationScoped
class TransactedJmsSender extends BaseJmsSender {
	@Inject
	@Transacted
	private JMSContext jmsContext;

	@Override
	JMSContext getJmsContext() {
		return jmsContext;
	}
}
