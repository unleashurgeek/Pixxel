package io.lgs.starbound.proxy;

import io.lgs.starbound.entity.Player;
import io.lgs.starbound.proxy.packets.Packet;
import io.lgs.starbound.proxy.packets.Packet7ClientConnect;
import io.lgs.starbound.proxy.packets.RawPacket;
import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.ByteArrayDataOutputStream;
import io.lgs.starbound.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ThreadForward extends Thread {
	private static final int BUFFER_SIZE = 1460;
	private final DataInputStream input;
    private final ByteArrayDataOutputStream output;
	private final ThreadClient client;
	private final boolean isToServer;
	
	private boolean isRunning = true;
	
	public ThreadForward(ThreadClient client, DataInputStream input, ByteArrayDataOutputStream output, boolean isToServer) {
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
		RawPacket pkt = null;
		try {
			int size;
			
			while ((size = input.read(buffer)) != -1) {
				System.out.println("Debug0" + isToServer);
				if (!isRunning)
					break;
				
				int buffer_pos = 0;
				
				while (buffer_pos < size) {
					byte[] tmp_buffer = Arrays.copyOfRange(buffer, buffer_pos, size);
					ByteArrayDataInput barr = new ByteArrayDataInput(tmp_buffer);
					
					System.out.println("Debug2" + isToServer);
					
					if (pkt == null || pkt.eop) {
						pkt = new RawPacket();
					}
					
					pkt.fetchPacket(barr);
					
					buffer_pos = barr.getPosition();
					
					barr = null;
					
					System.out.println(pkt.type + ":" + pkt.eop);
					
					if (!pkt.eop)
						break;
					
					System.out.println(pkt.type + ":" + pkt.data_length + ":" + Util.bytesToHex(pkt.data));
					
					// We should move this later, just for testing
					if (pkt.type == 7) {
						Packet7ClientConnect packet = (Packet7ClientConnect) Packet.readPacket(pkt, isToServer);
						
						Player player = new Player(
								packet.username,
								packet.uuid,
								packet.race,
								client);
						client.setPlayer(player);
						pkt.writeRawPacket(this.output);
						
						System.out.println("Debug3" + isToServer);
						
						continue;
					}
					
					System.out.println("Debug4" + isToServer);
					
					Packet packet = Packet.readPacket(pkt, isToServer);
					
					if (packet != null) {
						packet.processPacket(client.getPacketHandler());
					} else {
						pkt.writeRawPacket(this.output);
					}
				}
				
				System.out.println("Debug5" + isToServer);
			}
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Something went wrong and broke the forward thread(s) of "
							+ client.getClientSocket().getInetAddress());
			this.client.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
