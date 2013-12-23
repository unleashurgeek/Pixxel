package io.lgs.starbound.proxy;

import io.lgs.starbound.entity.Player;
import io.lgs.starbound.proxy.packets.Packet;
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

public class ThreadForward {
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
	
	public void start() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				byte[] buffer = new byte[BUFFER_SIZE];
				RawPacket pkt = null;
				String prefix = isToServer ? "C2S:" : "S2C:";
				try {
					int size;			
					while ((size = input.read(buffer)) != -1) {
						if (!isRunning)
							break;
						
						int buffer_pos = 0;
						
						while (buffer_pos < size) {
							byte[] tmp_buffer = Arrays.copyOfRange(buffer, buffer_pos, size);
							ByteArrayDataInput barr = new ByteArrayDataInput(tmp_buffer);
							
							if (pkt == null || pkt.eop) {
								pkt = new RawPacket();
							}
							
							pkt.fetchPacket(barr);
							
							buffer_pos = barr.getPosition();
							
							barr = null;
							
							if (!pkt.eop)
								break;
							
							Packet packet = Packet.readPacket(pkt, isToServer);
							
							if (isToServer) {
								System.out.println("Received C2S Packet");
								client.receiveFromClientQueue.add(packet);
							} else {
								System.out.println("Received S2C Packet");
								client.receiveFromServerQueue.add(packet);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.out
							.println("Something went wrong and broke the forward thread(s) on " + prefix + pkt.type + " of "
									+ client.getClientSocket().getInetAddress());
					client.disconnect();
				}
			}		
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				Packet pkt = null;
				
				while (isRunning) {
					String prefix = "";
					
					if (isToServer) {
						prefix = "C2S";
						pkt = client.take(client.receiveFromClientQueue);
					} else {
						prefix = "S2C";
						pkt = client.take(client.receiveFromServerQueue);
					}
					
					if (pkt == null)
						continue;
					
					System.out.println("Processing " + prefix + " Packet");
					
					pkt.processPacket(client.getPacketHandler());
					
					if (isToServer) {
						client.sendToServerQueue.add(pkt);
					} else {
						client.sendToClientQueue.add(pkt);
					}
				}
			}
			
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				Packet pkt = null;
				
				while (isRunning) {
					String prefix = "";
					
					if (isToServer) {
						prefix = "C2S";
						pkt = client.take(client.sendToServerQueue);
					} else {
						prefix = "S2C";
						pkt = client.take(client.sendToClientQueue);
					}
					
					if (pkt == null)
						continue;
					
					System.out.println("Sending " + prefix + " Packet");
					
					try {
						pkt.writePacket(output);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}).start();
	}
}
