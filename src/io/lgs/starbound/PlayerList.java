package io.lgs.starbound;

import io.lgs.starbound.entity.Player;
import io.lgs.starbound.file.BanList;
import io.lgs.starbound.proxy.ThreadClient;
import io.lgs.starbound.proxy.packets.Packet7ClientConnect;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.xml.internal.ws.server.ServerSchemaValidationTube;

public class PlayerList {
	//public  final List<Player> players = new CopyOnWriteArrayList<Player>();
	public  final List<ThreadClient> clients = new CopyOnWriteArrayList<ThreadClient>();
	private final BanList banList = Wrapper.getServer().getConfig().banFile();
	
	private Socket serverSocket = null;
	
	public PlayerList(StarboundServer starboundServer) {
		try {
			this.serverSocket = new Socket("127.0.0.1", 21024);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void attemptSocketConnect(Socket clientSocket) throws UnknownHostException, IOException {
		if (banList.getBans().contains(clientSocket.getInetAddress().getHostAddress()))
			return;
		ThreadClient client = new ThreadClient(clientSocket, serverSocket);
		clients.add(client);
		client.start();
	}
	
	public void attemptLogin(Packet7ClientConnect packet, ThreadClient client) {
		
		// TODO: Store ThreadClient
		// TODO: Rewrite Player initializer to add all params
		// TODO: Get rid of attemptSocketConnect
		Player player = new Player(packet.getUsername(), client);
		player.setUUID(packet.getUUID());
		
		players.add(player);
	}
	
	// TOOD: Add method that creates player from chat. That should allow ThreadClient to have been created by then.
	// TODO: Possibly remove ThreadClient from clients list after setting it to player to safe storage?
	// TODO: Write disconnect Method that deletes Thread then Player.
	
}
