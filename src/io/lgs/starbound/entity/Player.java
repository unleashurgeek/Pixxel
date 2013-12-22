package io.lgs.starbound.entity;

import io.lgs.starbound.Wrapper;
import io.lgs.starbound.proxy.ThreadClient;
import io.lgs.starbound.proxy.packets.Packet5ChatReceive;

import java.net.InetAddress;

public class Player {
	private final String username;
	private final ThreadClient client;
	private String UUID;
	private Boolean op = false;
	
	public Player(String username, ThreadClient client) {
		this.username = username;
		this.client = client;
	}
	
	public String getName() {
		return username;
	}
	
	public String getUUID() {
		return UUID;
	}
	
	// TODO: store UUID's in ClientThread or PlayerList
	public void setUUID(String UUID) {
		this.UUID = UUID;
	}
	
	public InetAddress getAddress() {
		return client.getClientSocket().getInetAddress();
	}
	
	public Boolean isOp() {
		return op;
	}
	
	public void setOp(Boolean op) {
		this.op = op;
	}
	
	public void kickPlayer() {
		
		// TODO: replace with sending disconnect packet.
		client.disconnect();
		Wrapper.getServer().playerList.disconnect();
		// Remove from Player lists?
		// send disconnect method in PlayerList?
	}
	
	public void sendMessage(String message) {
		client.sendPacketToClient(new Packet5ChatReceive(this.getName(), message));
		// Write sends. Main use of this will be for plugins using  
	}
}
