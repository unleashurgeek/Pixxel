package io.lgs.starbound.proxy;

import io.lgs.starbound.Wrapper;
import io.lgs.starbound.entity.Player;
import io.lgs.starbound.proxy.packets.Packet;
import io.lgs.starbound.proxy.packets.PacketHandler;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

public class ThreadClient extends Thread {
	
	private final Socket clientSocket;
	private final Socket serverSocket;
	private ServerStreams serverStreams = new ServerStreams();
	private ClientStreams clientStreams = new ClientStreams();
	private PacketHandler packetHandler;
	
	private ThreadForward forwardServer;
	private ThreadForward forwardClient;
	
	
	// TODO: Add a queue system
	private Queue<Packet> sendToClientQueue = new LinkedList<Packet>();
	private Queue<Packet> sendToServerQueue = new LinkedList<Packet>();
	
	private Queue<Packet> receiveFromClientQueue = new LinkedList<Packet>();
	private Queue<Packet> receiveFromServerQueue = new LinkedList<Packet>();
	
	private Player player;
	
	public ThreadClient(Socket clientSocket) throws UnknownHostException, IOException {
		this.clientSocket = clientSocket;
		this.serverSocket = new Socket("127.0.0.1", 21022);
		this.packetHandler = new PacketHandler(this);
	}
	
	public ThreadClient(Socket clientSocket, Socket serverSocket) throws UnknownHostException, IOException {
		this.clientSocket = clientSocket;
		this.serverSocket = serverSocket;
		this.packetHandler = new PacketHandler(this);
	}
	
	@Override
	public void run() {
		
		try {
			serverStreams.setInputStream(serverSocket.getInputStream());
			serverStreams.setOutputStream(serverSocket.getOutputStream());
			
			clientStreams.setInputStream(clientSocket.getInputStream());
			clientStreams.setOutputStream(clientSocket.getOutputStream());
			
			// Packets Client to Server
			forwardServer = new ThreadForward(this, clientStreams.getInputStream(), serverStreams.getOutputStream(), true);
			
			// Packets Server to Client
			forwardClient =  new ThreadForward(this, serverStreams.getInputStream(), clientStreams.getOutputStream(), false);
			
			// Start Forwards
			forwardServer.start();
			forwardClient.start();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not create Input and Output streams!");
		}
		
		
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public PacketHandler getPacketHandler() {
		return packetHandler;
	}
	
	public Socket getClientSocket() {
		return clientSocket;
	}
	
	public Socket getServerSocket() {
		return serverSocket;
	}
	
	public ServerStreams getServerStreams() {
		return serverStreams;
	}
	
	public ClientStreams getClientStreams() {
		return clientStreams;
	}
	
	public void sendPacketToClient(Packet packet) {
		try {
			packet.writePacket(clientStreams.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendPacketToServer(Packet packet) {
		try {
			packet.writePacket(clientStreams.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		Wrapper.getServer().playerList.disconnect(this);
		forwardServer.setRunning(false);
		forwardServer.setRunning(false);
	}
}