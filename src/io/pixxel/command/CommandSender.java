package io.pixxel.command;

import io.pixxel.PixxelServer;
import io.pixxel.permissions.Permissible;

public interface CommandSender extends Permissible {
	public void sendMessage(String sender, String message);
	
	public void sendMessage(String sender, String[] messages);
	
	public PixxelServer getServer();
	
	public String getName();
}
