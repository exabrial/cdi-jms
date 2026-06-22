package com.github.exabrial.cdi.jms.listeners.se.internal.model;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.jms.JMSContext;

import com.github.exabrial.cdi.jms.listeners.se.model.ListenerState;

import lombok.Data;

@Data
public class ListenerContext {
	private final JMSContext jmsContext;
	private final CreationalContext<?> creationalContext;
	private ListenerState listenerState;
}
