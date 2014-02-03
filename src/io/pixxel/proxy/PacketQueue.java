package io.pixxel.proxy;

import io.pixxel.Pixxel;
import io.pixxel.proxy.packets.Packet;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;


public class PacketQueue extends ConcurrentLinkedQueue<Packet> {
	private static final long serialVersionUID = 616455748783638601L;

	public PacketQueue() {
		super();
	}
	
	
	public void sendPacketToQueue(Packet packet) {
		try {
			this.add(packet);
		} catch(NullPointerException e) {
			Pixxel.getServer().getLogger().log(Level.SEVERE, "Packet was not able to be sent to Queue! Packet ID: " + packet.getPacketId());
		}
	}
	
	public Packet getNextPacket() {
		if (this.peek() == null)
			Pixxel.getServer().getLogger().log(Level.SEVERE, "Packet at head of queue is null!");
		return this.poll();
	}
}
