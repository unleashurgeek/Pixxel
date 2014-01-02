package io.pixxel.plugin;

import io.pixxel.event.Event;
import io.pixxel.event.EventException;
import io.pixxel.event.EventPriority;
import io.pixxel.event.Listener;

// TODO: GOOD
public class TimedRegisteredListener extends RegisteredListener {

	private int count;
	private long totalTime;
	private Class<? extends Event> eventClass;
	private boolean multiple = false;
	
	public TimedRegisteredListener(Listener listener, EventExecutor executor,
			EventPriority priority, Plugin plugin, boolean ignoreCancelled) {
		super(listener, executor, priority, plugin, ignoreCancelled);
	}
	
	@Override
	public void callEvent(Event event) throws EventException {
		if (event.isAsynchronous()) {
			super.callEvent(event);
			return;
		}
		count++;
		Class<? extends Event> newEventClass = event.getClass();
		if (this.eventClass == null) {
			this.eventClass = newEventClass;
		} else if (!this.eventClass.equals(newEventClass)) {
			multiple = true;
			this.eventClass = getCommonSuperclass(newEventClass, this.eventClass).asSubclass(Event.class);
		}
	}

	private static Class<?> getCommonSuperclass(Class<?> class1, Class<?> class2) {
		while (!class1.isAssignableFrom(class2)) {
			class1 = class1.getSuperclass();
		}
		return class1;
	}
	
	public void reset() {
		count = 0;
		totalTime = 0;
	}
	
	public int getCount() {
		return count;
	}
	
	public long getTotalTime() {
		return totalTime;
	}
	
	public Class<? extends Event> getEventClass() {
		return eventClass;
	}
	
	public boolean hasMultiple() {
		return multiple;
	}
}
