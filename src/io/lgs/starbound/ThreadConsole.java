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
			String line;
			int i = 0;
			while ((line = starboundConsole.readLine()) != null) {
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getVersion() {
		return version;
	}
	
}
