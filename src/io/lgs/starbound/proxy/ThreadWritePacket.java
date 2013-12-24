package io.lgs.starbound.proxy;

import io.lgs.starbound.proxy.packets.Packet;
import io.lgs.starbound.util.ByteArrayDataOutputStream;

import java.io.IOException;

public class ThreadWritePacket extends Thread {
	private final PacketQueue queue;
	private final ByteArrayDataOutputStream output;
	
	private boolean isRunning = true;
	
	public ThreadWritePacket(PacketQueue queue, ByteArrayDataOutputStream output) {
		this.queue = queue;
		this.output = output;
	}
	
	@Override
	public void run() {
		while (isRunning) {
			if (queue.isEmpty())
				continue;
			
			Packet p = queue.getNextPacket();
			if (p == null)
				continue;
			
			try {
				System.out.println("Writting Packet");
				p.writePacket(output);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Could not write packet to Output Stream!");
			}
		}
		queue.clear();
		System.out.println("Write Packet Stopped");
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setRunning(boolean value) {
		this.isRunning = value;
	}
}
