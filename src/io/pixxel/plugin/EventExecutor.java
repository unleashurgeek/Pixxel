package io.pixxel.plugin;

import io.pixxel.event.Event;
import io.pixxel.event.EventException;
import io.pixxel.event.Listener;

// TODO: GOOD
public interface EventExecutor {
	public void execute(Listener listener, Event event) throws EventException;
}
