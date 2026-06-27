package com.github.exabrial.cdi.jms.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;

import com.github.exabrial.cdi.common.api.qualifier.NonTransacted;

@NonTransacted
@ApplicationScoped
class NonTransactedJmsSender extends BaseJmsSender {
	@Inject
	@NonTransacted
	private JMSContext jmsContext;

	@Override
	JMSContext getJmsContext() {
		return jmsContext;
	}
}
