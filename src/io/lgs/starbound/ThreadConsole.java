package io.lgs.starbound;

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
				if (line.startsWith("Warn: ") && !Wrapper.getServer().getConfig().showWarnings())
					showLine = false;
				else if (line.startsWith("liquid error") && !Wrapper.getServer().getConfig().showLiquidError())
					showLine = false;
				else if (line.startsWith("Error: ") && !Wrapper.getServer().getConfig().showError())
					showLine = false;
				
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
