package com.github.exabrial.cdi.jms.transport.api;

import java.io.Serializable;
import java.util.List;

import com.github.exabrial.cdi.jms.transport.wiremodel.model.JmsTransportReply;
import com.github.exabrial.cdi.jms.transport.wiremodel.model.exception.TimeoutWhileWaitingForReplyException;

public interface JmsTransport {
	<K extends Serializable> K sendAndWaitForReply(Serializable payload, Class<K> replyType) throws TimeoutWhileWaitingForReplyException;

	<K extends Serializable> K sendAndWaitForReply(Serializable payload, Class<K> replyType, long replyTimeout)
			throws TimeoutWhileWaitingForReplyException;

	<K extends Serializable> List<JmsTransportReply<K>> sendAndGatherReplies(Serializable payload, Class<K> replyType, long waitTime);
}