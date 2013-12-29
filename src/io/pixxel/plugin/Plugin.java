package io.pixxel.plugin;

import io.pixxel.PixxelServer;

import java.io.File;

import com.sun.istack.internal.logging.Logger;

public interface Plugin {
	
	public File getDataFolder();
	
	
	public PluginDescription getDescription();
	
	public PluginLoader getPluginLoader();
	
	public PixxelServer getServer();
	
	public boolean isEnabled();
	
	public void onDisable();
	
	public void onLoad();
	
	public void onEnable();
	
	public Logger getLogger();
	
	public String getName();
}
