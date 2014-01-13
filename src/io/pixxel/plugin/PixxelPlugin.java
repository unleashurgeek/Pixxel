package io.pixxel.plugin;

import io.pixxel.PixxelServer;
import io.pixxel.command.Command;
import io.pixxel.command.CommandSender;

import java.io.File;
import java.util.logging.Logger;

// TODO: GOOD
public class PixxelPlugin implements Plugin {
	private boolean isEnabled = false;
	private PluginLoader loader = null;
	private PixxelServer server = null;
	private PluginDescription description = null;
	private File dataFolder = null;
	private ClassLoader classLoader = null;
	private PluginLogger logger = null;
	
	public PixxelPlugin() {
		final ClassLoader classLoader = this.getClass().getClassLoader();
		if (!(classLoader instanceof PluginClassLoader)) {
			throw new IllegalStateException("PixxelPlugin requires " + PluginClassLoader.class.getName());
		}
		((PluginClassLoader) classLoader).initialize(this);
	}
	
	protected PixxelPlugin(final PluginLoader loader, final PluginDescription description, final File dataFolder) {
		final ClassLoader classLoader = this.getClass().getClassLoader();
		if (classLoader instanceof PluginClassLoader) {
			throw new IllegalStateException("Cannot use initialization constructor at runtime!");
		}
		init(loader, loader.server, description, dataFolder, classLoader);
	}
	
	@Override
	public File getDataFolder() {
		return dataFolder;
	}

	@Override
	public final PluginDescription getDescription() {
		return description;
	}

	@Override
	public PixxelServer getServer() {
		return server;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}
	
	protected final ClassLoader getClassLoader() {
		return classLoader;
	}
	
	protected final void setEnabled(final boolean enabled) {
		if (isEnabled != enabled) {
			isEnabled = enabled;
			
			if (isEnabled) {
				onEnable();
			} else {
				onDisable();
			}
		}
	}
	
	final void init(PluginLoader loader, PixxelServer server, PluginDescription description, File dataFolder, ClassLoader classLoader) {
		this.loader = loader;
		this.server = server;
		this.description = description;
		this.dataFolder = dataFolder;
		this.classLoader = classLoader;
		this.logger = new PluginLogger(this);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return false;
	}
	
	public void onLoad() {}

	public void onDisable() {}

	public void onEnable() {}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public String getName() {
		return description.getName();
	}

	@Override
	public PluginLoader getPluginLoader() {
		return loader;
	}
	
	@Override
	public String toString() {
		return description.getFullName();
	}

	public static <T extends PixxelPlugin> T getPlugin(Class<T> clss) {
		if (clss == null) {
			return null;
		}
		if (!PixxelPlugin.class.isAssignableFrom(clss)) {
			throw new IllegalArgumentException(clss + " does not extend " + PixxelPlugin.class);
		}
		final ClassLoader cl = clss.getClassLoader();
		if (!(cl instanceof PluginClassLoader)) {
			throw new IllegalArgumentException(clss + " is not initialized by " + PluginClassLoader.class);
		}
		PixxelPlugin plugin = ((PluginClassLoader) cl).plugin;
		if (plugin == null)
			throw new IllegalStateException("Cannot get plugin for " + clss + "from a static initializer");
		return clss.cast(plugin);
	}
	
	public static PixxelPlugin getProvidingPlugin(Class<?> clss) {
		if (clss == null) {
			return null;
		}
		
		final ClassLoader cl = clss.getClassLoader();
		if (!(cl instanceof PluginClassLoader)) {
			throw new IllegalArgumentException(clss + " is not provided by " + PluginClassLoader.class);
		}
		PixxelPlugin plugin = ((PluginClassLoader) cl).plugin;
		if (plugin == null) {
			throw new IllegalStateException("Cannot get plugin for " + clss + "from a static initializer");
		}
		return plugin;
	}
}
