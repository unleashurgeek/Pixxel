package io.pixxel;

import io.pixxel.entity.PixxelConsoleCommandSender;
import io.pixxel.file.ServerProperties;
import io.pixxel.proxy.ThreadProxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class StarboundServer {
	
	private Process starbound;
	private final String version = "1.0b";
	private ThreadProxy proxy;
	public PixxelConsoleCommandSender console;
	
	public StarboundServer(ProcessBuilder starboundBuilder, ServerProperties properties) {
		BufferedReader starboundConsole;
		try {
			//starbound = starboundBuilder.start();
			//starboundConsole = new BufferedReader(new InputStreamReader(starbound.getInputStream()));
			
			// Start Proxy
			proxy = new ThreadProxy();
			proxy.start();
			
			PlayerList playerList = new PlayerList(this, properties);
			new PixxelServer(this, playerList, properties);
			
			Pixxel.getServer().getLogger().log(Level.INFO, "Starting Pixxel version " + version);
			
			// Started Console Monitor Thread
			//ThreadConsole console = new ThreadConsole(starboundConsole);
			//console.start();
			//version = console.getVersion();
			
			// TODO: Move to separate thread.
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			for (String line; (line = br.readLine()) != null;) {
				Pixxel.getServer().dispatchCommand(console, line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getVersion() {
		return version;
	}
	
	public void shutdown() {
		try {
			proxy.kill();
		} catch (IOException e) {
			e.printStackTrace();
		}
		starbound.destroy();
		Pixxel.getServer().playerList.clients.clear();
		// TODO: Clear all data sheats (Players connected, etc)
	}
}
