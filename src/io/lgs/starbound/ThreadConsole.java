package io.lgs.starbound;

import io.lgs.starbound.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;

public class ThreadConsole extends Thread {
	private BufferedReader starboundConsole;
	private String version; 
	
	public ThreadConsole(BufferedReader starboundConsole) {
		this.starboundConsole = starboundConsole;
	}
	
	@Override
	public void run()
	{
		try {
			for (String line; (line = starboundConsole.readLine()) != null;) {
				boolean showLine = true; 
				if (line.startsWith("Warn: ") && !Pixxel.getServer().getConfig().showWarnings())
					showLine = false;
				else if (line.startsWith("liquid error") && !Pixxel.getServer().getConfig().showLiquidError())
					showLine = false;
				else if (line.startsWith("Error: ") && !Pixxel.getServer().getConfig().showError())
					showLine = false;
				
				// TODO: Replace with PacketHandler
				if (line.endsWith("disconnected")) {
					String[] parts = line.split("'");
					StringBuilder username = new StringBuilder();
					for (int i = 1; i < parts.length -1; i++) {
						username.append(parts[i]);
					}
					
					Player player = Pixxel.getServer().getPlayer(username.toString());
					player.kickPlayer();
					System.out.println("player kicked?");
				}
				
				if (showLine)
					System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getVersion() {
		return version;
	}
	
}
