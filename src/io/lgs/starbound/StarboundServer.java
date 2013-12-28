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
			
			// Started Console Monitor Thread
			//ThreadConsole console = new ThreadConsole(starboundConsole);
			//console.start();
			//version = console.getVersion();
			
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
