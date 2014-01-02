package io.pixxel.plugin;

import io.pixxel.event.*;

// TODO: GOOD
public class RegisteredListener {
	private final Listener listener;
	private final EventPriority priority;
	private final Plugin plugin;
	private final EventExecutor executor;
	private final boolean ignoreCancelled;
	
	public RegisteredListener(final Listener listener, final EventExecutor executor, final EventPriority priority, final Plugin plugin, final boolean ignoreCancelled) {
		this.listener = listener;
		this.executor = executor;
		this.priority = priority;
		this.plugin = plugin;
		this.ignoreCancelled = ignoreCancelled;
	}
	
	public Listener getListener() {
		return listener;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public EventPriority getPriority() {
		return priority;
	}
	
	public void callEvent(final Event event) throws EventException {
		if (event instanceof Cancellable) {
			if (((Cancellable) event).isCancelled() && isIgnoringCancelled()) {
				return;
			}
		}
		executor.execute(listener, event);
	}
	
	public boolean isIgnoringCancelled() {
		return ignoreCancelled;
	}
}
