package com.github.exabrial.cdi.jms.listeners.se.model;

/**
 * State of a tracked listener. null means the handle is not tracked (killed or never started).
 */
public enum ListenerState {
	RUNNING, PAUSED;
}
