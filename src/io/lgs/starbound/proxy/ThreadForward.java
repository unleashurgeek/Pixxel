package io.lgs.starbound.proxy;

import io.lgs.starbound.proxy.packets.Packet;
import io.lgs.starbound.proxy.packets.RawPacket;
import io.lgs.starbound.util.ByteArrayDataInput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ThreadForward extends Thread {
	private static final int BUFFER_SIZE = 1460;
	private final DataInputStream input;
	private final DataOutputStream output;
	private final ThreadClient client;
	private final boolean isToServer;
	
	private boolean isRunning = true;
	
	public ThreadForward(ThreadClient client, DataInputStream input, DataOutputStream output, boolean isToServer) {
		this.client = client;
		this.output = output;
		this.input = input;
		this.isToServer = isToServer;
	}
	
	public void setRunning(boolean value) {
		isRunning = value;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[BUFFER_SIZE];
		RawPacket pkt = new RawPacket();
		Packet packet = null;
		boolean firstRun = true;
		try {
			int size;
			while ((size = input.read(buffer)) != -1) {
				if (!isRunning)
					break;
				
				int round_length = 0;
				ByteArrayDataInput barr = new ByteArrayDataInput(buffer);
				
				while (round_length < size) {
					if (firstRun || pkt.eop) {
						firstRun = false;
						pkt = Packet.fetchRawPacket(barr);
					} else {
						pkt = Packet.fetchRawPacket(barr, pkt);
					}

					round_length = barr.getPosition();
					if (!pkt.eop || pkt.zlib)
						break;
					
					packet = Packet.readPacket(pkt, isToServer);
				}
				if (packet != null) {
					System.out.println(Arrays.toString(buffer));
					packet.processPacket(client.getPacketHandler());
				} else {
					output.write(buffer, 0, size);
					output.flush();
				}
			}
				
				
				
				/*while (pkt.round_length < bufferSize) {
					if (pkt.eop) {
						pkt = Packet.fetchRawPacket(new ByteArrayDataInput(
								buffer));
					} else {
						pkt = Packet.fetchRawPacket(new ByteArrayDataInput(
								buffer), pkt);
					}

					if (!pkt.eop)
						break;
				}*/
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Something went wrong and broke the forward thread(s) of "
							+ client.getClientSocket().getInetAddress());
			this.client.disconnect();
		}
	}
}
