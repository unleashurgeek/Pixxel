package io.pixxel.plugin;

import java.io.File;
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
import java.util.regex.Pattern;

import javax.xml.bind.Marshaller.Listener;

import org.w3c.dom.events.Event;
import org.yaml.snakeyaml.error.YAMLException;

import io.pixxel.PixxelServer;
import io.pixxel.event.EventHandler;

public final class PluginLoader {
	final PixxelServer server;
	private final Pattern[] fileFilters = new Pattern[] {Pattern.compile("\\.jar$"), };
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	private final Map<String, PluginClassLoader> loaders = new LinkedHashMap<String, PluginClassLoader>();
	
	public PluginLoader(PixxelServer server) {
		this.server = server;
	}
	
	public Plugin loadPlugin(File file) {
		if (file == null) {
			System.out.println("File cannot be null!");
			return null;
		}
		
		if (!file.exists()) {
			System.out.println(file.getPath() + " is non Existant!");
			return null;
		}
		
		PluginDescription description;
		description = getPluginDescription(file);
		
		File dataFolder = new File(file.getParentFile(), description.getName());
		
		if (dataFolder.exists() && !dataFolder.isDirectory()) {
			System.out.println(String.format("Datafolder: '%s' for %s (%s) exists and is not a directory",
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
				unkownDependency
			}
			PluginClassLoader current = loaders.get(pluginName);
			if (current == null) {
				unkownDependency
			}
		}
		
		PluginClassLoader loader;
		try {
			loader = new PluginClassLoader(this, getClass().getClassLoader(), description, dataFolder, file);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		loaders.put(description.getName(), loader);
		
		return loader.plugin;
	}
	
	private PluginDescription getPluginDescription(File file) {
		JarFile jar = null;
		InputStream stream = null;
		
		try {
			jar = new JarFile(file);
			JarEntry entry = jar.getJarEntry("plugin.yml");
			
			if (entry == null) {
				System.out.println("Plugin.yml does not exist!");
				return null;
			}
			
			stream = jar.getInputStream(entry);
			
			return new PluginDescription(stream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (YAMLException e) {
			e.printStackTrace();
			return null;
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
		if (plugin == null || listener == null) {
			return null;
		}
		
		boolean useTimings = server.getPluginManager().useTimings();
		Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<>();
		Set<Method> methods;
		try {
			Method[] publicMethods = listener.getClass().getMethods();
			methods = new HashSet<Method>(publicMethods.length, Float.MAX_VALUE);
			for (Method method : publicMethods) {
				methods.add(method);
			}
			
			for (Method method : listener.getClass().getDeclaredMethods()) {
				methods.add(method);
			}
		} catch (NoClassDefFoundError e) {
			System.out.println("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");
			return ret;
		}
		
		for (final Method method : methods) {
			final EventHandler eh = method.getAnnotation(EventHandler.class);
			if (eh == null) continue;
			final Class<?> checkClass;
			if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
				System.out.println(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
				continue;
			}
			final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
			method.setAccessible(true);
			Set<RegisteredListener> eventSet = ret.get(eventClass);
			if (eventSet == null) {
				eventSet = new HashSet<ResisteredListener>();
				ret.put(eventClass, eventSet);
			}
			
			for (Class<?> clss = eventClass; Event.class.isAssignableFrom(cls); cls = cls.getSuperClass()) {
				if (clss.getAnnotation(Deprecated.class != null)) {
					Warning warning = clss.getAnnotation(Warning.class);
					WarningState warningState = server.getWarningState();
					if (!warningState.printFor(warning)) {
						break;
					}
					
					System.out.println(String.format(
                                    "\"%s\" has registered a listener for %s on method \"%s\", but the event is Deprecated." +
                                    " \"%s\"; please notify the authors %s.",
                                    plugin.getDescription().getFullName(),
                                    clss.getName(),
                                    method.toGenericString(),
                                    (warning != null && warning.reason().length() != 0) ? warning.reason() : "Server performance will be affected",
                                    Arrays.toString(plugin.getDescription().getAuthors().toArray())),
                            warningState == WarningState.ON ? new AuthorNagException(null) : null);
					break;
				}
			}
			
			EventExecutor executor = new EventExecutor() {
				public void execute(Listener, listener, Event event) {
					try {
						if (!eventClass.isAssignableFrom(event.getClass())) {
							return;
						}
						method.invoke(listener, event);
					} catch (InvocationTargetException e) {
						
					} catch (Throwable t) {
						
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
			System.out.println("Plugin is not associated with Pixxel");
			return;
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
				System.out.println("Error occured while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)");
			}
			
			server.getPluginManager().callEvent(new PluginEnableEvent(plugin));
		}
	}
	
	public void disablePlugin(Plugin plugin) {
		if (!(plugin instanceof PixxelPlugin)) {
			System.out.println("Plugin is not associated with Pixxel");
			return;
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
				System.out.println("Error ocured while disabling " + plugin.getDescription().getFullName());
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
