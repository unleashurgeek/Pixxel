package io.pixxel.proxy;

import io.pixxel.proxy.packets.Packet;

import java.util.concurrent.ConcurrentLinkedQueue;


public class PacketQueue extends ConcurrentLinkedQueue<Packet> {
	private static final long serialVersionUID = 616455748783638601L;

	public PacketQueue() {
		super();
	}
	
	
	public void sendPacketToQueue(Packet packet) {
		try {
			this.add(packet);
		} catch(NullPointerException e) {
			System.out.println("ERROR: Packet was not able to be sent to Queue! Packet ID: " + packet.getPacketId());
		}
	}
	
	public Packet getNextPacket() {
		if (this.peek() == null)
			System.out.println("ERROR: Packet at head of queue is null!");
		return this.poll();
	}
}
