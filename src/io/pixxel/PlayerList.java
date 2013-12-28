package io.pixxel;

import io.pixxel.file.BanList;
import io.pixxel.file.ServerProperties;
import io.pixxel.proxy.ThreadClient;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerList {
	public  final List<ThreadClient> clients = new CopyOnWriteArrayList<ThreadClient>();
	private final BanList banList;
	
	public PlayerList(StarboundServer starboundServer, ServerProperties properties) {
		banList = properties.banFile();
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void attemptLogin(Socket clientSocket) throws UnknownHostException, IOException {
		//if (!banList.getBans().isEmpty() && banList.getBans() != null && banList.getBans().contains(clientSocket.getInetAddress().getHostAddress()))
			//return;
		
		ThreadClient client = new ThreadClient(clientSocket);
		clients.add(client);
		client.start();
	}
	
	public void disconnect(ThreadClient client) {
		clients.remove(client);
	}
}
