package com.github.exabrial.cdi.jms.transport.wiremodel.model;

import lombok.Data;

/**
 * Optional destination parameters: destination name extension and/or message selector.
 *
 * @author jonathan.fisher
 */
@Data
public class DestinationParameters {
	private final String destinationExtension;
	private final String selector;
}
