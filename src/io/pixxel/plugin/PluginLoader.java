package io.pixxel.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.error.YAMLException;

import io.pixxel.Pixxel;
import io.pixxel.PixxelServer;
import io.pixxel.event.Event;
import io.pixxel.event.EventException;
import io.pixxel.event.EventHandler;
import io.pixxel.event.Listener;

public final class PluginLoader {
	final PixxelServer server;
	private final Pattern[] fileFilters = new Pattern[] {Pattern.compile("\\.jar$"), };
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	private final Map<String, PluginClassLoader> loaders = new LinkedHashMap<String, PluginClassLoader>();
	
	public PluginLoader(PixxelServer server) {
		this.server = server;
	}
	
	public Plugin loadPlugin(File file) throws InvalidPluginException {
		if (file == null) {
			throw new IllegalArgumentException("File cannot be null");
		}
		
		if (!file.exists()) {
			throw new InvalidPluginException(new FileNotFoundException(file.getPath() + " does not exist"));
		}
		
		PluginDescription description;
		try {
			description = getPluginDescription(file);
		} catch (InvalidDescriptionException e) {
			throw new InvalidPluginException(e);
		}
		
		File dataFolder = new File(file.getParentFile(), description.getName());
		
		if (dataFolder.exists() && !dataFolder.isDirectory()) {
			throw new InvalidPluginException(String.format("Datafolder: '%s' for %s (%s) exists and is not a directory",
					dataFolder,
					description.getName(),
					file
			));
		}
		
		List<String> depend = description.getDepend();
		if (depend == null) {
			depend = new ArrayList<>();
		}
		
		for (String pluginName : depend) {
			if (loaders == null) {
				throw new UnknownDependencyException(pluginName);
			}
			PluginClassLoader current = loaders.get(pluginName);
			if (current == null) {
				throw new UnknownDependencyException(pluginName);
			}
		}
		
		PluginClassLoader loader;
		try {
			loader = new PluginClassLoader(this, getClass().getClassLoader(), description, dataFolder, file);
		} catch (InvalidPluginException e) {
			throw e;
		} catch (Throwable t) {
			throw new InvalidPluginException(t);
		}
		
		loaders.put(description.getName(), loader);
		
		return loader.plugin;
	}
	
	public PluginDescription getPluginDescription(File file) throws InvalidDescriptionException {
		if (file == null)
			throw new IllegalArgumentException("File cannot be null!");
		
		JarFile jar = null;
		InputStream stream = null;
		
		try {
			jar = new JarFile(file);
			JarEntry entry = jar.getJarEntry("plugin.yml");
			
			if (entry == null) {
				throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain plugin.yml"));
			}
			
			stream = jar.getInputStream(entry);
			
			return new PluginDescription(stream);
		} catch (IOException e) {
			throw new InvalidDescriptionException(e);
		} catch (YAMLException e) {
			throw new InvalidDescriptionException(e);
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (IOException e) {
				}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public Pattern[] getPluginFileFilters() { 
		return fileFilters.clone();
	}
	
	Class<?> getClassByName(final String name) {
		Class<?> cachedClass = classes.get(name);
		
		if (cachedClass != null) {
			return cachedClass;
		} else {
			for (String current : loaders.keySet()) {
				PluginClassLoader loader = loaders.get(current);
				
				try {
					cachedClass = loader.findClass(name, false);
				} catch (ClassNotFoundException e) { }
				if (cachedClass != null) {
					return cachedClass;
				}
			}
		}
		return null;
	}
	
	void setClass(final String name, final Class<?> clss) {
		if (!classes.containsKey(name)) {
			classes.put(name, clss);
		}
	}
	
	private void removeClass(String name) {
		classes.remove(name);
	}
	
	public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListener(Listener listener, final Plugin plugin) {
		if (plugin == null)
			throw new IllegalArgumentException("Plugin cannot be null!");
		if (listener == null)
			throw new IllegalArgumentException("Listener cannot be null!");
		
		boolean useTimings = server.getPluginManager().useTimings();
		Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<>();
		Set<Method> methods;
		try {
			Method[] publicMethods = listener.getClass().getMethods();
			methods = new HashSet<>(publicMethods.length, Float.MAX_VALUE);
			for (Method method : publicMethods)
				methods.add(method);
			
			for (Method method : listener.getClass().getDeclaredMethods())
				methods.add(method);
		} catch (NoClassDefFoundError e) {
			plugin.getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + "because " +  e.getMessage() + " does not exist.");
			return ret;
		}
		
		for (final Method method : methods) {
			final EventHandler eh = method.getAnnotation(EventHandler.class);
			if (eh == null) continue;
			final Class<?> checkClass;
			if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
				plugin.getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
				continue;
			}
			final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
			method.setAccessible(true);
			Set<RegisteredListener> eventSet = ret.get(eventClass);
			if (eventSet == null) {
				eventSet = new HashSet<>();
				ret.put(eventClass, eventSet);
			}
			
			for (Class<?> clss = eventClass; Event.class.isAssignableFrom(clss); clss = clss.getSuperclass()) {
				if (clss.getAnnotation(Deprecated.class) != null) {
					plugin.getLogger().log(Level.WARNING,
							String.format(
                                    "\"%s\" has registered a listener for %s on method \"%s\", but the event is Deprecated." +
                                    " \"Server performance will be affected\"; please notify the authors %s.",
                                    plugin.getDescription().getFullName(),
                                    clss.getName(),
                                    method.toGenericString(),
                                    Arrays.toString(plugin.getDescription().getAuthors().toArray())),
                                    new AuthorNagException(null));
					break;
					
				}
			}
			
			EventExecutor executor = new EventExecutor() {
				
				@Override
				public void execute(Listener listener, Event event) throws EventException {
					try {
						if (!eventClass.isAssignableFrom(event.getClass())) {
							return;
						}
						method.invoke(listener, event);
					} catch (InvocationTargetException ex) {
						throw new EventException(ex.getCause());
					} catch (Throwable t) {
						throw new EventException(t);
					}
				}
			};
			if (useTimings)
				eventSet.add(new TimedRegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
			else
				eventSet.add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
		}
		
		return ret;
	}
	
	public void enablePlugin(final Plugin plugin) {
		if (!(plugin instanceof PixxelPlugin)) {
			throw new InvalidPluginException("Plugin is not associated with this PluginLoader");
		}
		
		if (!plugin.isEnabled()) {
			plugin.getLogger().info("Enabling " + plugin.getDescription().getFullName());
			
			PixxelPlugin pPlugin = (PixxelPlugin) plugin;
			
			String pluginName = pPlugin.getDescription().getName();
			
			if (!loaders.containsKey(pluginName)) {
				loaders.put(pluginName, (PluginClassLoader) pPlugin.getClassLoader());
			}
			
			try {
				pPlugin.setEnabled(true);
			} catch (Throwable ex) {
				Pixxel.getServer().getLogger().log(Level.SEVERE, "Error occured while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)");
			}
			
			server.getPluginManager().callEvent(new PluginEnableEvent(plugin));
		}
	}
	
	public void disablePlugin(Plugin plugin) {
		if (!(plugin instanceof PixxelPlugin)) {
			throw new InvalidPluginException("Plugin is not associated with this PluginLoader");
		}
		
		if (plugin.isEnabled()) {
			String message = "Disabling " + plugin.getDescription().getFullName();
			plugin.getLogger().info(message);
			
			server.getPluginManager().callEvent(new PluginDisableEvent(plugin));
			
			PixxelPlugin pPlugin = (PixxelPlugin) plugin;
			ClassLoader cloader = pPlugin.getClassLoader();
			
			try {
				pPlugin.setEnabled(false);
			} catch (Throwable ex) {
				Pixxel.getServer().getLogger().log(Level.SEVERE, "Error ocured while disabling " + plugin.getDescription().getFullName());
			}
			
			loaders.remove(pPlugin.getDescription().getName());
			
			if (cloader instanceof PluginClassLoader) {
				PluginClassLoader loader = (PluginClassLoader) cloader;
				Set<String> names = loader.getClasses();
				
				for (String name : names) {
					removeClass(name);
				}
			}
		}
	}
}
