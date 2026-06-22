package com.github.exabrial.cdi.jms.transport.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.Message;

import org.slf4j.Logger;

import com.github.exabrial.cdi.jms.api.qualifier.NonTransacted;
import com.github.exabrial.cdi.jms.transport.wiremodel.model.JmsTransportErrorReply;
import com.github.exabrial.cdi.nanoscopes.boundaryscoped.api.interceptor.Boundary;

@Boundary
@ApplicationScoped
class JmsErrorReplyTransport {
	@Inject
	private Logger log;
	@Inject
	@NonTransacted
	private JmsTransportResponder responder;

	public void replyWithError(final Message originalMessage, final Exception exception) {
		log.trace("replyWithError() exception:{}", exception.getMessage());
		final JmsTransportErrorReply errorReply = new JmsTransportErrorReply(exception);
		responder.reply(originalMessage, errorReply);
		log.trace("replyWithError() complete");
	}
}
