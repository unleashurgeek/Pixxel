package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.entity.Player;
import io.lgs.starbound.proxy.ThreadClient;

public class PacketHandler {
	
	private final ThreadClient client;
	private final Player player;
	
	public PacketHandler(ThreadClient client) {
		this.client = client;
		this.player = client.getPlayer();
	}
	
	public void handleChatSend(Packet11ChatSend packet) {
		
	}
}
