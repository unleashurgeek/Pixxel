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
	
	
	
	public void attemptLogin(final Socket clientSocket) throws UnknownHostException, IOException {
		if (banList.getBans() != null && banList.getBans().contains(clientSocket.getInetAddress().getHostAddress())) {
			System.out.println("Here!");
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
						
						if (breakThreads)
							break;
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
				RawPacket pkt = new RawPacket();
				boolean firstRun = true;
				try {
					for (int size; (size = clientSocket.getInputStream().read(buffer)) != -1;) {
						int round_length = 0;
						ByteArrayDataInput barr = new ByteArrayDataInput(buffer);
						
						while (round_length < size) {
							if (firstRun || pkt.eop) {
								firstRun = false;
								pkt = Packet.fetchRawPacket(barr);
							} else {
								pkt = Packet.fetchRawPacket(barr, pkt);
							}

							if (!pkt.eop || pkt.zlib)
								break;

							round_length = barr.getPosition();
						}
						
						if (pkt.type == 7 && pkt.eop) {
							System.out.println("Perhaps here?");
							Packet7ClientConnect packet = (Packet7ClientConnect) Packet.readPacket(pkt, true);
							final ThreadClient client = new ThreadClient(clientSocket, server);
							Player player = new Player(packet.username, packet.uuid, packet.race, client);
							client.setPlayer(player);
							
							server.getOutputStream().write(buffer, 0, size);
							server.getOutputStream().flush();
							clients.add(client);
							
							breakThreads = true;
							
							client.start();
							if (breakThreads)
								break;
						} else {
							server.getOutputStream().write(buffer, 0, size);
                            server.getOutputStream().flush();
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
