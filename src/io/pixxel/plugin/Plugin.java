package io.pixxel.plugin;

import io.pixxel.PixxelServer;
import io.pixxel.command.CommandExecutor;

import java.io.File;
import java.util.logging.Logger;

// TODO: GOOD
public interface Plugin extends CommandExecutor {
	
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
