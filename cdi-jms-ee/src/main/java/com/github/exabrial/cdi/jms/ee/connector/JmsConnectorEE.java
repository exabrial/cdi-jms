package com.github.exabrial.cdi.jms.ee.connector;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

import com.github.exabrial.cdi.jms.internal.api.JmsConnector;

/**
 * Default EE connector. Produces nothing by default.
 *
 * @author jonathan.fisher
 */
@Default
@ApplicationScoped
public class JmsConnectorEE implements JmsConnector {

}