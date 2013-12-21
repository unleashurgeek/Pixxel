package io.lgs.starbound.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerProperties {

	private final Properties serverProperties;
	private final File propertiesFile;
	
	//---- Server Properties Data ----	
	
	/** Location of starbound_server(.exe) */
	private String serverLocation;
	
	// TODO: Replace with StarboundConfig.java for Quick Access.
	/** Location of starbound.config */
	private String starboundConfig;
	
	/** Boolean to show "warn:" data in console */
	private Boolean showWarnings;
	
	/** Boolean to show "liquid error" data in console */
	private Boolean showLiquidError;
	
	/** Boolean to show "Error:" data in console */
	private Boolean showError;
	
	/** File Admins.txt */
	private File adminFile;
	
	/** File Bans.txt */
	private File banFile;
	
	public ServerProperties(File file) {
		this.serverProperties = new Properties();
		this.propertiesFile = file;
	}
	
	public void load() throws IOException, FileNotFoundException {
		if (!propertiesFile.exists()) {
			propertiesFile.mkdirs();
			propertiesFile.createNewFile();
			
			// ---- Default Config Settings ----
			serverProperties.setProperty("serverLocation", "starbound_server.exe");
			serverProperties.setProperty("starboundConfigLocation", "starbound.config");
			serverProperties.setProperty("showWarnings", "false");
			serverProperties.setProperty("showLiquidError", "false");
			serverProperties.setProperty("showError", "true");
			serverProperties.setProperty("adminFile", "Admins.txt");
			serverProperties.setProperty("banFile", "Bans.txt");
			
			serverProperties.store(new FileOutputStream(propertiesFile), null);
		}
		serverProperties.load(new FileInputStream(propertiesFile));
		
		// ---- Load Properties Data ----
		this.serverLocation   = serverProperties.getProperty("serverLocation", "starbound_server.exe");
		this.starboundConfig  = serverProperties.getProperty("starboundConfigLocation", "starbound.config");
		this.showWarnings     = Boolean.valueOf(serverProperties.getProperty("showWarnings", "false"));
		this.showLiquidError  = Boolean.valueOf(serverProperties.getProperty("showLiquidError", "false"));
		this.showError        = Boolean.valueOf(serverProperties.getProperty("showError", "true"));
		this.adminFile        = new File(serverProperties.getProperty("adminFile", "Admins.txt"));
		this.banFile          = new File(serverProperties.getProperty("banFile", "Bans.txt"));
	}
	
	public String serverLocation() {
		return serverLocation;
	}
	
	public String starboundConfig() {
		return starboundConfig;
	}
	
	public File adminFile() {
		return adminFile;
	}
	
	public File banFile() {
		return banFile;
	}
	
	public Boolean showWarnings() {
		return showWarnings;
	}
	
	public Boolean showLiquidError() {
		return showLiquidError;
	}
	
	public Boolean showError() {
		return showError;
	}
}