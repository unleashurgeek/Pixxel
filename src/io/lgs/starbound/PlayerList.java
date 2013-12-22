package io.lgs.starbound;

import io.lgs.starbound.entity.Player;
import io.lgs.starbound.file.BanList;
import io.lgs.starbound.file.ServerProperties;
import io.lgs.starbound.proxy.ThreadClient;
import io.lgs.starbound.proxy.packets.Packet;
import io.lgs.starbound.proxy.packets.Packet7ClientConnect;
import io.lgs.starbound.util.ByteArrayDataInput;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerList {
	public  final List<ThreadClient> clients = new CopyOnWriteArrayList<ThreadClient>();
	private final BanList banList;
	
	private Socket serverSocket = null;
	
	public PlayerList(StarboundServer starboundServer, ServerProperties properties) {
		banList = properties.banFile();
		try {
			this.serverSocket = new Socket("127.0.0.1", 21024);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void attemptLogin(final Socket clientSocket) throws UnknownHostException, IOException {
		if (banList.getBans().contains(clientSocket.getInetAddress().getHostAddress()))
			return;
		final ThreadClient client = new ThreadClient(clientSocket, serverSocket);
		
		 new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] buffer = new byte[1460];
				try {
					for (int size; (size = serverSocket.getInputStream().read(buffer)) != -1;) {
						if (buffer[0] == (byte)0x07) {
							Packet7ClientConnect packet = (Packet7ClientConnect) Packet.readPacket(new ByteArrayDataInput(buffer), true);
							
							Player player = new Player(packet.username, packet.uuid, packet.race, client);
							client.setPlayer(player);
							
							clientSocket.getOutputStream().write(buffer, 0, size);
							clientSocket.getOutputStream().flush();
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		clients.add(client);
		client.start();
		
		// TODO: Check if Player is Opped.
		// TODO: Possibly changed List from clients to Players for security?
	}
	
	public void disconnect(ThreadClient client) {
		clients.remove(client);
	}
}
