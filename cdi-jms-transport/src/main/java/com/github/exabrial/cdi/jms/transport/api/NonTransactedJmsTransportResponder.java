package com.github.exabrial.cdi.jms.transport.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;

import com.github.exabrial.cdi.common.api.qualifier.NonTransacted;

@NonTransacted
@ApplicationScoped
class NonTransactedJmsTransportResponder extends BaseJmsTransportResponder {
	@Inject
	@NonTransacted
	private JMSContext jmsContext;

	@Override
	JMSContext getJmsContext() {
		return jmsContext;
	}
}
