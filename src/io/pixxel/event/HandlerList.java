package io.pixxel.event;

import io.pixxel.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

public class HandlerList {
	private volatile RegisteredListener[] handlers = null;
	private final EnumMap<EventPriority, ArrayList<RegisteredListener>> handlerSlots;
	private static ArrayList<HandlerList> allLists = new ArrayList<HandlerList>();
	
	public static void bakeAll() {
		synchronized (allLists) {
			for (HandlerList h : allLists) {
				h.bake();
			}
		}
	}
	
	public static void unregisterAll() {
		synchronized (allLists) {
			for (HandlerList h : allLists) {
				synchronized (h) {
					for (List<RegisteredListener> list : h.handlerSlots.values()) {
						list.clear();
					}
					h.handlers = null;
				}
			}
		}
	}
	
	public static void unregisterAll(Plugin plugin) {
		synchronized (allLists) {
			for (HandlerList h : allLists) {
				h.unregister(plugin);
			}
		}
	}
	
	public HandlerList() {
		handlerSlots = new EnumMap<EventPriority, ArrayList<RegisteredListener>>(EventPriority.class);
		for (EventPriority o : EventPriority.values()) {
			handlerSlots.put(o, new ArrayList<RegisteredListener>());
		}
		synchronized (allLists) {
			allLists.add(this);
		}
	}
	
	public synchronized void register(RegisteredListener listener) {
		if (handlerSlots.get(listener.getPriority()).contains(listener))
			throw new IllegalStateException("This listener is already regitered to priority " + listener.getPriority().toString());
		handlers = null;
		handlerSlots.get(listener.getPriority()).add(listener);
	}
	
	public void registerAll(Collection<RegisteredListener> listeners) {
		for (RegisteredListener listener : listeners) {
			register(listener);
		}
	}
	
	public synchronized void unregister(RegisteredListener listener) {
		if (handlerSlots.get(listener.getPriority()).remove(listener)) {
			handlers = null;
		}
	}
	
	public synchronized void unregister(Plugin plugin) {
		boolean changed = false;
		for (List<RegisteredListener> list : handlerSlots.values()) {
			for (ListIterator<RegisteredListener> i = list.listIterator(); i.hasNext();) {
				if (i.next().getPlugin().equals(plugin)) {
					i.remove();
					changed = true;
				}
			}
		}
		if (changed) handlers = null;
	}
	
	public synchronized void unregister(Listener listener) {
		boolean changed = false;
		for (List<RegisteredListener> list : handlerSlots.values()) {
			for (ListIterator<RegisteredListener> i = list.listIteretator(); i.hasNext();) {
				if (i.next().getListener().equals(listener)) {
					i.remove();
					changed = true;
				}
			}
		}
		if (changed) handlers = null;
	}
	
	public synchronized void bake() {
		if (handlers != null) return;
		List<RegisteredListener> entries = new ArrayList<RegisteredListener>();
		for (Entry<EventPriority, ArrayList<RegisteredListener>> entry : handlerSlots.entrySet()) {
			entries.addAll(entry.getValue());
		}
		handlers = entries.toArray(new RegisteredListener[entries.size()]);
	}
	
	public RegisteredListener[] getRegisteredListeners() {
		RegisteredListener[] handlers;
		
		while ((handlers = this.handlers) == null) bake();
		return handlers;
	}
	
	public static ArrayList<RegisteredListener> getRegisteredListeners(Plugin plugin) {
		ArrayList<RegisteredListener> listeners = new ArrayList<RegisteredListener>();
		synchronized (allLists) {
			for (HandlerList h : allLists) {
				synchronized (h) {
					for (List<RegisteredListener> list : h.handlerSlots.values()) {
						for (RegisteredListener listener : list) {
							if (listener.getPlugin().equals(plugin)) {
								listeners.add(listener);
							}
						}
					}
				}
			}
		}
		return listeners;
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<HandlerList> getHandlerLists() {
		synchronized(allLists) {
			return (ArrayList<HandlerList>) allLists.clone();
		}
	}
}
