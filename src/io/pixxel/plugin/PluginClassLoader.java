package io.pixxel.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//TODO: GOOD
public class PluginClassLoader extends URLClassLoader {
	private PluginLoader loader;
	private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	private PluginDescription description;
	private File dataFolder;
	private File file;
	PixxelPlugin plugin;
	private PixxelPlugin  pluginInit;
	private IllegalStateException pluginState;
	
	public PluginClassLoader(final PluginLoader loader, final ClassLoader parent, final PluginDescription description, final File dataFolder, final File file) throws MalformedURLException, InstantiationException, InvalidPluginException {
		super(new URL[] {file.toURI().toURL()}, parent);
		if (loader == null) {
			throw new IllegalArgumentException("PluginLoader cannot be null!");
		}
		
		this.loader = loader;
		this.description = description;
		this.dataFolder = dataFolder;
		this.file = file;
		
		try {
			Class<?> jarClass;
			try {
				jarClass = Class.forName(description.getMain(), true, this);
			} catch (ClassNotFoundException e) {
				throw new InvalidPluginException("Cannot find main class '" + description.getMain() + "'", e);
			}
			
			Class<? extends PixxelPlugin> pluginClass;
			try {
				pluginClass = jarClass.asSubclass(PixxelPlugin.class);
			} catch (ClassCastException e) {
				throw new InvalidPluginException("main class '" + description.getMain() + "' does not extend PixxelPlugin", e);
			}
			
			plugin = pluginClass.newInstance();
		} catch (IllegalAccessException e) {
			throw new InvalidPluginException("No public constructor", e);
		} catch (InstantiationException e) {
			throw new InvalidPluginException("Abnormal plugin type", e);
		}
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return findClass(name, true);
	}
	
	Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
		if (name.startsWith("io.pixxel")) {
			throw new ClassNotFoundException(name);
		}
		Class<?> result = classes.get(name);
		if (result == null) {
			if (checkGlobal) {
				result = loader.getClassByName(name);
			}
			
			if (result == null) {
				result = super.findClass(name);
			
				if (result != null) {
					loader.setClass(name, result);
				}
			}
		
			classes.put(name, result);
		}
		
		return result;
	}
	
	Set<String> getClasses() {
		return classes.keySet();
	}
	
	synchronized void initialize (PixxelPlugin pixxelPlugin) {
		if (pixxelPlugin == null)
			throw new IllegalArgumentException("PixxelPlugin cannot be Null!");
		if (pixxelPlugin.getClass().getClassLoader() != this)
			throw new IllegalArgumentException("Cannot Initialize plugin outside of this class loader");
		
		if (this.plugin != null || this.pluginInit != null) {
			throw new IllegalArgumentException("Plugin already intialized!", pluginState);
		}
		
		pluginState = new IllegalStateException("Initial Initialization");
		this.pluginInit = pixxelPlugin;
		
		pixxelPlugin.init(loader, loader.server, description, dataFolder, file, this);
	}
}
