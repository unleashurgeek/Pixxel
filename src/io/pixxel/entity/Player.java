package io.pixxel.entity;

import io.pixxel.proxy.ThreadClient;
import io.pixxel.proxy.packets.Packet11ChatSend;
import io.pixxel.util.ChatColor;

import java.net.InetAddress;

public class Player {
	private final String username;
	private final ThreadClient client;
	private final String UUID;
	private final String race;
	private boolean op = false;
	
	public Player(String username, String UUID, String race, ThreadClient client) {
		this.username = username;
		this.UUID = UUID;
		this.race = race;
		this.client = client;
	}
	
	public String getName() {
		return username;
	}
	
	public String getUUID() {
		return UUID;
	}
	
	public String getRace() {
		return race;
	}
	
	public InetAddress getAddress() {
		return client.getClientSocket().getInetAddress();
	}
	
	public boolean isOp() {
		return op;
	}
	
	public void setOp(boolean op) {
		this.op = op;
	}
	
	public void kickPlayer() {
		// TODO: replace with sending disconnect packet.
		client.disconnect();
	}
	
	public void sendMessage(String message, ChatColor color) {
		client.getPacketHandler().sendPacketToClient(new Packet11ChatSend(message));
		// Write sends. Main use of this will be for plugins using  
	}
}
