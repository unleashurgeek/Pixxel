package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.entity.Player;
import io.lgs.starbound.proxy.ThreadClient;

public class PacketHandler {
	
	private final ThreadClient client;
	
	public PacketHandler(ThreadClient client) {
		this.client = client;
	}
	
	public void handleConnect(Packet7ClientConnect packet) {
		Player p = new Player(packet.username, packet.uuid, packet.race, client);
		// TODO: Check if he is admin
		
		client.setPlayer(p);
		sendPacketToServer(packet);
	}
	
	public void handleChatSend(Packet11ChatSend packet) {
		
	}
	
	public void handleGeneric(Packet0Generic packet) {
		if (packet.isToServer)
			sendPacketToServer(packet);
		else
			sendPacketToClient(packet);
	}
	
	public void sendPacketToClient(Packet packet) {
		this.client.getToClientQueue().sendPacketToQueue(packet);
	}
	
	public void sendPacketToServer(Packet packet) {
		this.client.getToServerQueue().sendPacketToQueue(packet);
	}
}
