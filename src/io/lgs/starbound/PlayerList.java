package io.lgs.starbound;

import io.lgs.starbound.entity.Player;
import io.lgs.starbound.file.BanList;
import io.lgs.starbound.file.ServerProperties;
import io.lgs.starbound.proxy.ClientStreams;
import io.lgs.starbound.proxy.ThreadClient;
import io.lgs.starbound.proxy.packets.Packet;
import io.lgs.starbound.proxy.packets.Packet7ClientConnect;
import io.lgs.starbound.proxy.packets.RawPacket;
import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.Util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerList {
	public  final List<ThreadClient> clients = new CopyOnWriteArrayList<ThreadClient>();
	private final BanList banList;
	private boolean breakThreads = false;
	
	public PlayerList(StarboundServer starboundServer, ServerProperties properties) {
		banList = properties.banFile();
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void connect(final Socket clientSocket) throws UnknownHostException, IOException {
		final Socket server = new Socket("127.0.0.1", 21022);		
		ThreadClient client = new ThreadClient(clientSocket, server);
		clients.add(client);
		client.start();
	}
	
	public void attemptLogin(final Socket clientSocket) throws UnknownHostException, IOException {
		if (banList.getBans() != null && !banList.getBans().isEmpty() && banList.getBans().contains(clientSocket.getInetAddress().getHostAddress())) {
			return;
		}
		
		final Socket server = new Socket("127.0.0.1", 21024);		
		
		// To Client
		new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] buffer = new byte[1460];
				try {
					for (int size; (size = server.getInputStream().read(buffer)) != -1;) {
						clientSocket.getOutputStream().write(buffer, 0, size);
						clientSocket.getOutputStream().flush();
						
						if (breakThreads) {
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		// To Server
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				byte[] buffer = new byte[1460];
				RawPacket pkt = null;
				boolean firstRun = true;
				
				try {
					for (int size; (size = clientSocket.getInputStream().read(buffer)) != -1;) {
						
						int buffer_pos = 0;
						
						ThreadClient client = new ThreadClient(clientSocket, server);

						while (buffer_pos < size) {
							byte[] tmp_buffer = Arrays.copyOfRange(buffer, buffer_pos, size);
							ByteArrayDataInput barr = new ByteArrayDataInput(tmp_buffer);
							
							if (pkt == null || pkt.eop) {
								pkt = new RawPacket();
							}
							
							pkt.fetchPacket(barr);
							
							buffer_pos = barr.getPosition();
							
							barr = null;
							
							System.out.println("Waiting..." + (pkt.data_length - pkt.data_pos));
							
							if (!pkt.eop)
								break;
							
							System.out.println("Packet finished!");
							
							if (pkt.type == 7) {
								System.out.println("Found it!");
								Packet7ClientConnect packet = (Packet7ClientConnect) Packet.readPacket(pkt, true);
								Player player = new Player(packet.username, packet.uuid, packet.race, client);
								client.setPlayer(player);
								
								server.getOutputStream().write(tmp_buffer, 0, buffer_pos);
								server.getOutputStream().flush();
								clients.add(client);
								
								breakThreads = true;

								// removed break here. We shouldnt stop reading if there are packets left in the buffer
							} else {
								System.out.println("Wrong one!");
								
								server.getOutputStream().write(tmp_buffer, 0, buffer_pos);
								server.getOutputStream().flush();
							}
						}
						
						if (breakThreads) {
							System.out.println("Leaving login now!");
							client.start();
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		// TODO: Check if Player is Opped.
		// TODO: Possibly changed List from clients to Players for security?
	}
	
	public void disconnect(ThreadClient client) {
		clients.remove(client);
	}
}
