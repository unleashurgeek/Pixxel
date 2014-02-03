package io.pixxel.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.pixxel.Pixxel;
import io.pixxel.PixxelServer;
import io.pixxel.command.Command;
import io.pixxel.command.CommandMap;
import io.pixxel.command.PluginCommandYamlParser;
import io.pixxel.event.Event;
import io.pixxel.event.EventPriority;
import io.pixxel.event.HandlerList;
import io.pixxel.event.Listener;
import io.pixxel.permissions.Permissible;
import io.pixxel.permissions.Permission;
import io.pixxel.permissions.PermissionDefault;

public final class PluginManager {
	private final PixxelServer server;
	private final Map<Pattern, PluginLoader> fileAssociations = new HashMap<Pattern, PluginLoader>();
	private final List<Plugin> plugins = new ArrayList<Plugin>();
	private final Map<String, Plugin> lookupNames = new HashMap<String, Plugin>();
	private final CommandMap commandMap;
	private final Map<String, Permission> permissions = new HashMap<String, Permission>();
	private final Map<Boolean, Set<Permission>> defaultPerms = new LinkedHashMap<Boolean, Set<Permission>>();
	private final Map<String, Map<Permissible, Boolean>> permSubs = new HashMap<String, Map<Permissible, Boolean>>();
	private final Map<Boolean, Map<Permissible, Boolean>> defSubs = new HashMap<Boolean, Map<Permissible, Boolean>>();
	private boolean useTimings = false;
	
	public PluginManager(PixxelServer server, CommandMap commandMap) {
		this.server = server;
		this.commandMap = commandMap;
		
		defaultPerms.put(true, new HashSet<Permission>());
		defaultPerms.put(false, new HashSet<Permission>());
	}
	
	public void registerInterface(Class<? extends PluginLoader> loader) throws IllegalArgumentException {
		PluginLoader instance;
		
		if (PluginLoader.class.isAssignableFrom(loader)) {
			Constructor<? extends PluginLoader> constructor;
			
			 try {
				 constructor = loader.getConstructor(PixxelServer.class);
				 instance = constructor.newInstance(server);
			 } catch (NoSuchMethodException e) {
				 String className = loader.getName();
				 
	             throw new IllegalArgumentException(String.format("Class %s does not have a public %s(Server) constructor", className, className), e);
			 } catch (Exception e) {
				 throw new IllegalArgumentException(String.format("Unexpected exception %s while attempting to construct a new instance of %s", e.getClass().getName(), loader.getName()), e);
			 }
		} else {
			throw new IllegalArgumentException(String.format("Class %s does not implement interface PluginLoader", loader.getName()));
		}
		
		Pattern[] patterns = instance.getPluginFileFilters();
		
		synchronized (this) {
			for (Pattern pattern : patterns) {
				fileAssociations.put(pattern, instance);
			}
		}
	}
	
	public Plugin[] loadPlugins(File directory) {
		if (directory == null || !directory.isDirectory()) {
			throw new IllegalArgumentException("Directory is eith null or not a directory!");
		}
		
		List<Plugin> result = new ArrayList<Plugin>();
		Set<Pattern> filters = fileAssociations.keySet();
		
		Map<String, File> plugins = new HashMap<String, File>();
		Set<String> loadedPlugins = new HashSet<String>();
		Map<String, Collection<String>> dependencies = new HashMap<String, Collection<String>>();
		Map<String, Collection<String>> softDependencies = new HashMap<String, Collection<String>>();
		
		for (File file : directory.listFiles()) {
			PluginLoader loader = null;
			for (Pattern filter : filters) {
				Matcher match = filter.matcher(file.getName());
				if (match.find()) {
					loader = fileAssociations.get(filter);
				}
			}
			
			if (loader == null) continue;
			
			PluginDescription description = null;
			try {
				description = loader.getPluginDescription(file);
			} catch (InvalidDescriptionException e) {
				Pixxel.getServer().getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'");
			}
			
			plugins.put(description.getName(), file);
			
			Collection<String> softDependencySet = description.getSoftDepend();
			if (softDependencySet != null) {
				if (softDependencies.containsKey(description.getName())) {
					softDependencies.get(description.getName()).addAll(softDependencySet);
				} else {
					softDependencies.put(description.getName(), new LinkedList<String>(softDependencySet));
				}
			}
			
			Collection<String> dependencySet = description.getDepend();
			if (dependencySet != null) {
				dependencies.put(description.getName(), new LinkedList<String>(softDependencySet));
			}
			
			Collection<String> loadBeforeSet = description.getLoadBefore();
			if (loadBeforeSet != null) {
				for (String loadBeforeTarget : loadBeforeSet) {
					if (softDependencies.containsKey(loadBeforeTarget)) {
						softDependencies.get(loadBeforeTarget).add(description.getName());
					} else {
						Collection<String> shortSoftDependency = new LinkedList<String>();
						shortSoftDependency.add(description.getName());
						softDependencies.put(loadBeforeTarget, shortSoftDependency);
					}
				}
			}
		}
		
		while (!plugins.isEmpty()) {
			boolean missingDependency = true;
			Iterator<String> pluginIterator = plugins.keySet().iterator();
			
			while (pluginIterator.hasNext()) {
				String plugin = pluginIterator.next();
				
				if (dependencies.containsKey(plugin)) {
					Iterator<String> dependencyIterator = dependencies.get(plugin).iterator();
					
					while (dependencyIterator.hasNext()) {
						String dependency = dependencyIterator.next();
						
						if (loadedPlugins.contains(dependency)) {
							dependencyIterator.remove();
						} else if (!plugins.containsKey(dependency)) {
							missingDependency = false;
							File file = plugins.get(plugin);
							pluginIterator.remove();
							softDependencies.remove(plugin);
							dependencies.remove(plugin);
							
							Pixxel.getServer().getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'");
							break;
						}
					}
					
					if (dependencies.containsKey(plugin) && dependencies.get(plugin).isEmpty()) {
						dependencies.remove(plugin);
					}
				}
				
				if (softDependencies.containsKey(plugin)) {
					Iterator<String> softDependencyIterator = softDependencies.get(plugin).iterator();
					
					while (softDependencyIterator.hasNext()) {
						String softDependency = softDependencyIterator.next();
						
						if (!plugins.containsKey(softDependency)) {
							softDependencyIterator.remove();
						}
					}
					
					if (softDependencies.get(plugin).isEmpty()) {
						softDependencies.remove(plugin);
					}
				}
				
				if (!(dependencies.containsKey(plugin) || softDependencies.containsKey(plugin)) && plugins.containsKey(plugin)) {
					File file = plugins.get(plugin);
					pluginIterator.remove();
					missingDependency = false;
					
					try {
						result.add(loadPlugin(file));
						loadedPlugins.add(plugin);
						continue;
					} catch (InvalidPluginException e) {
						Pixxel.getServer().getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'");
					}
				}
			}
			
			if (missingDependency) {
				pluginIterator = plugins.keySet().iterator();
				
				while (pluginIterator.hasNext()) {
					String plugin = pluginIterator.next();
					
					if (!dependencies.containsKey(plugin)) {
						softDependencies.remove(plugin);
						missingDependency = false;
						File file = plugins.get(plugin);
						pluginIterator.remove();
						
						try {
							result.add(loadPlugin(file));
							loadedPlugins.add(plugin);
							break;
						} catch (InvalidPluginException e) {
							Pixxel.getServer().getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'");
						}
					}
				}
				
				if (missingDependency) {
					softDependencies.clear();
					dependencies.clear();
					Iterator<File> failedPluginIterator = plugins.values().iterator();
					
					while (failedPluginIterator.hasNext()) {
						File file = failedPluginIterator.next();
						failedPluginIterator.remove();
						Pixxel.getServer().getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "': circular dependency detected");
					}
				}
			}
		}
		
		return result.toArray(new Plugin[result.size()]);
	}
	
	public synchronized Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
		if (file == null) {
			throw new IllegalArgumentException("File cannnot be null when loading a plugin!");
		}
		
		Set<Pattern> filters = fileAssociations.keySet();
		Plugin result = null;
		
		for (Pattern filter : filters) {
			String name = file.getName();
			Matcher match = filter.matcher(name);
			
			if (match.find()) {
				PluginLoader loader = fileAssociations.get(filter);
				
				result = loader.loadPlugin(file);
			}
		}
		
		if (result != null) {
			plugins.add(result);
			lookupNames.put(result.getDescription().getName(), result);
		}
		
		return result;
	}
	
	public synchronized Plugin getPlugin(String name) {
		return lookupNames.get(name);
	}
	
	public synchronized Plugin[] getPlugins() {
		return plugins.toArray(new Plugin[0]);
	}
	
	public boolean isPluginEnabled(Plugin plugin) {
		if ((plugin != null) && (plugins.contains(plugin))) {
			return plugin.isEnabled();
		} else {
			return false;
		}
	}
	
	public void enablePlugin(final Plugin plugin) {
		if (!plugin.isEnabled()) {
			List<Command> pluginCommands = PluginCommandYamlParser.parse(plugin);
			
			if (!pluginCommands.isEmpty()) {
				commandMap.registerAll(plugin.getDescription().getName(), pluginCommands);
			}
			
			try {
				plugin.getPluginLoader().enablePlugin(plugin);
			} catch (Throwable t) {
				Pixxel.getServer().getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while enabling " + plugin.getDescription().getFullName() + "(Is it up to date?)");
			}
			
			HandlerList.bakeAll();
		}
	}
	
	public void disablePlugins() {
		Plugin[] plugins = getPlugins();
		for (int i = plugins.length - 1; i >= 0; i--) {
			disablePlugin(plugins[i]);
		}
	}
	
	public void disablePlugin(final Plugin plugin) {
		if (plugin.isEnabled()) {
			plugin.getPluginLoader().disablePlugin(plugin);
			 HandlerList.unregisterAll(plugin);
		}
	}
	
	public void clearPlugins() {
		synchronized (this) {
			disablePlugins();
			plugins.clear();
			lookupNames.clear();
			HandlerList.unregisterAll();
			fileAssociations.clear();
			permissions.clear();
			defaultPerms.get(true).clear();
			defaultPerms.get(false).clear();
		}
	}
	
	public void callEvent(Event event) {
		if (event.isAsynchronous()) {
			if (Thread.holdsLock(this)) {
				throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from inside syncrhonized code.");
			}
			/*if (server.isPrimaryThread()) {
				throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from primary server thread");
			}*/
			fireEvent(event);
		} else {
			synchronized (this) {
				fireEvent(event);
			}
		}
	}
	
	private void fireEvent(Event event) {
		HandlerList handlers = event.getHandlers();
		RegisteredListener[] listeners = handlers.getRegisteredListeners();
		
		for (RegisteredListener registration : listeners) {
			if (!registration.getPlugin().isEnabled()) {
				continue;
			}
			
			try {
				registration.callEvent(event);
			} catch (AuthorNagException e) {
				Plugin plugin = registration.getPlugin();
				Pixxel.getServer().getLogger().log(Level.WARNING, String.format("Nag Author(s): '%s' of '%s' about the following: %s",
						plugin.getDescription().getAuthors(), plugin.getDescription().getFullName(), e.getMessage()));
			} catch (Throwable t) {
				Pixxel.getServer().getLogger().log(Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getPlugin().getDescription().getFullName());
			}
		}
	}
	
	public void registerEvents(Listener listener, Plugin plugin) {
		if (!plugin.isEnabled()) {
			throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
		}
		
		for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.getPluginLoader().createRegisteredListener(listener, plugin).entrySet()) {
			getEventListeners(getRegistrationClass(entry.getKey())).registerAll(entry.getValue());
		}
	}
	
	public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin) {
		registerEvent(event, listener, priority, executor, plugin, false);
	}
	
	public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin, boolean ignoreCancelled) {
		if (listener == null) {
			throw new IllegalArgumentException("Listener cannot be null");
		}
		if (priority == null) {
			throw new IllegalArgumentException("EventPriority cannot be null");
		}
		if (executor == null) {
			throw new IllegalArgumentException("EventExecutor cannot be null");
		}
		if (plugin == null) {
			throw new IllegalArgumentException("Plugin cannot be null");
		}
		
		if (!plugin.isEnabled()) {
			throw new IllegalPluginAccessException("Plugin attempted to register " + event + " while not enabled");
		}
		
		if (useTimings) {
			getEventListeners(event).register(new TimedRegisteredListener(listener, executor, priority, plugin, ignoreCancelled));
		} else {
			getEventListeners(event).register(new RegisteredListener(listener, executor, priority, plugin, ignoreCancelled));
		}
	}
	
	private HandlerList getEventListeners(Class<? extends Event> type) {
		try {
			Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
			method.setAccessible(true);
			return (HandlerList)method.invoke(null);
		} catch (Exception e) {
			throw new IllegalPluginAccessException(e.toString());
		}
	}
	
	private Class<? extends Event> getRegistrationClass(Class<? extends Event> clss) {
		try {
			clss.getDeclaredMethod("getHandlerList");
			return clss;
		} catch (NoSuchMethodException e) {
			if (clss.getSuperclass() != null
					&& !clss.getSuperclass().equals(Event.class)
					&& Event.class.isAssignableFrom(clss.getSuperclass())) {
				return getRegistrationClass(clss.getSuperclass().asSubclass(Event.class));
			} else {
				 throw new IllegalPluginAccessException("Unable to find handler list for event " + clss.getName());
			}
		}
	}
	
	public Permission getPermission(String name) {
		return permissions.get(name.toLowerCase());
	}
	
	public void addPermission(Permission perm) {
		String name = perm.getName().toLowerCase();
		
		if (permissions.containsKey(name)) {
			throw new IllegalArgumentException("The permission " + name + " is already defined!");
		}
		
		permissions.put(name, perm);
		calculatePermissionDefault(perm);
	}
	
	public Set<Permission> getDefaultPermissions(boolean op) {
		return new HashSet<Permission>(defaultPerms.get(op));
	}
	
	public void removePermission(Permission perm) {
		removePermission(perm.getName());
	}
	
	public void removePermission(String name) {
		permissions.remove(name.toLowerCase());
	}
	
	public void recalculatePermissionDefaults(Permission perm) {
		if (permissions.containsKey(perm)) {
			defaultPerms.get(true).remove(perm);
			defaultPerms.get(false).remove(perm);
			
			calculatePermissionDefault(perm);
		}
	}
	
	private void calculatePermissionDefault(Permission perm) {
		if ((perm.getDefault() == PermissionDefault.OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
			defaultPerms.get(true).add(perm);
			dirtyPermissibles(true);
		}
		if ((perm.getDefault() == PermissionDefault.NOT_OP) || (perm.getDefault() == PermissionDefault.FALSE)) {
			defaultPerms.get(false).add(perm);
			dirtyPermissibles(false);
		}
	}
	
	private void dirtyPermissibles(boolean op) {
		Set<Permissible> permissibles = getDefaultPermSubscriptions(op);
		
		for (Permissible p : permissibles) {
			p.recalculatePermissions();
		}
	}
	
	public void subscribeToPermission(String permission, Permissible permissible) {
		String name = permission.toLowerCase();
		Map<Permissible, Boolean> map = permSubs.get(name);
		
		if (map == null) {
			map = new WeakHashMap<Permissible, Boolean>();
			permSubs.put(name, map);
		}
		
		map.put(permissible, true);
	}
	
	public void unsubscribeFromPermission(String permission, Permissible permissible) {
		String name = permission.toLowerCase();
		Map<Permissible, Boolean> map = permSubs.get(name);
		
		if (map != null) {
			map.remove(permissible);
			
			if (map.isEmpty()) {
				permSubs.remove(name);
			}
		}
	}
	
	public Set<Permissible> getPermissionSubscriptions(String permission) {
		String name = permission.toLowerCase();
		Map<Permissible, Boolean> map = permSubs.get(name);
		
		if (map == null) {
			return null;
		} else {
			return new HashSet<Permissible>(map.keySet());
		}
	}
	
	public void subscribeToDefaultPerms(boolean op, Permissible permissible) {
		Map<Permissible, Boolean> map = defSubs.get(op);
		
		if (map == null) {
			map = new WeakHashMap<Permissible, Boolean>();
			defSubs.put(op, map);
		}
		
		map.put(permissible, true);
	}
	
	public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) {
		Map<Permissible, Boolean> map = defSubs.get(op);
		
		if (map != null) {
			map.remove(permissible);
			
			if (map.isEmpty()) {
				defSubs.remove(op);
			}
		}
	}
	
	public Set<Permissible> getDefaultPermSubscriptions(boolean op) {
		Map<Permissible, Boolean> map = defSubs.get(op);
		
		if (map == null) {
			return null;
		} else {
			return new HashSet<Permissible>(map.keySet());
		}
	}
	
	public Set<Permission> getPermissions() {
		return new HashSet<Permission>(permissions.values());
	}
	
	public boolean useTimings() {
		return useTimings;
	}
	
	public void useTimings(boolean use) {
		useTimings = use;
	}
}
