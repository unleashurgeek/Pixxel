package io.pixxel.proxy;

import io.pixxel.proxy.packets.Packet;
import io.pixxel.proxy.packets.RawPacket;
import io.pixxel.util.ByteArrayDataInput;
import io.pixxel.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ThreadReadPacket extends Thread {
	private final ThreadClient client;
	private final InputStream input;
	private final boolean isToServer;
	
	private boolean isRunning = true;
	
	/**
	 * Read Thread for reading a socket input stream and storing read packets to packet queue for writing.
	 * 
	 * @param input The Input stream to read packets from.
	 * @param queue The PacketQueue to send the processed packet to.
	 * @param isToServer The potential direction the packet is going, true if going to Server, false if Client.
	 */
	public ThreadReadPacket(ThreadClient client, InputStream input, boolean isToServer) {
		this.client = client;
		this.input = input;
		this.isToServer = isToServer;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[Util.BUFFER_SIZE];
		RawPacket pkt = null;
		try {
			int size;
			while (isRunning && (size = input.read(buffer)) != -1) {
				byte[] temp_buffer = Arrays.copyOfRange(buffer, 0, size);
				if (temp_buffer[0] == (byte)0x07) {
					System.out.println(temp_buffer.length);
					System.out.println(Arrays.toString(temp_buffer));
				}
				ByteArrayDataInput barr = new ByteArrayDataInput(temp_buffer);
				
				if (pkt == null || pkt.eop) {
					pkt = new RawPacket();
				}
				
				pkt.fetchPacket(barr);
				
				barr = null;
				
				if (!pkt.eop)
					continue;
				
				System.out.println(pkt.type);
				
				Packet packet = Packet.readPacket(pkt, isToServer);
				packet.processPacket(client.getPacketHandler());
			}
			input.close();
			System.out.println("Reader Broken");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setRunning(boolean value) {
		this.isRunning = value;
	}
}
