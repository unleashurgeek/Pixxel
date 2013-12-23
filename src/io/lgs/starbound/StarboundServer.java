package io.lgs.starbound;

import io.lgs.starbound.file.ServerProperties;
import io.lgs.starbound.proxy.ThreadProxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StarboundServer {
	
	private Process starbound;
	private String version;
	private ThreadProxy proxy;
	
	public StarboundServer(ServerProperties properties) {
		BufferedReader starboundConsole = null;
		
		try {
			if (!properties.detachedMode()) {
				ProcessBuilder pb = new ProcessBuilder(properties.serverLocation());
				pb.redirectErrorStream(true);
				starbound = pb.start();
				starboundConsole = new BufferedReader(new InputStreamReader(starbound.getInputStream()));
			}
			
			// Start Proxy
			proxy = new ThreadProxy();
			proxy.start();
			
			PlayerList playerList = new PlayerList(this, properties);
			new AegisServer(this, playerList, properties);
			
			if (!properties.detachedMode()) {
				// Started Console Monitor Thread
				ThreadConsole console = new ThreadConsole(starboundConsole);
				console.start();
				version = console.getVersion();
			}
		} catch (IOException e) {
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
		Wrapper.getServer().playerList.clients.clear();
		// TODO: Clear all data sheats (Players connected, etc)
	}
}
