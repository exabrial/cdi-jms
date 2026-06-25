package com.github.exabrial.cdi.jms.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;

import com.github.exabrial.cdi.jms.api.qualifier.NonTransacted;

@NonTransacted
@ApplicationScoped
class NonTransactedTopicProducerFactory extends BaseTopicProducerFactory {
	@Inject
	@NonTransacted
	private JMSContext jmsContext;

	@Override
	JMSContext getJmsContext() {
		return jmsContext;
	}
}
