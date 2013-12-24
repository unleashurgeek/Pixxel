package io.lgs.starbound.proxy;

import io.lgs.starbound.Wrapper;
import io.lgs.starbound.entity.Player;
import io.lgs.starbound.proxy.packets.PacketHandler;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ThreadClient extends Thread {
	
	private final Socket clientSocket;
	private final Socket serverSocket;
	
	private final PacketQueue toServerQueue;
	private final PacketQueue toClientQueue;
	
	private ServerStreams serverStreams = new ServerStreams();
	private ClientStreams clientStreams = new ClientStreams();
	
	// Server Read and Write Threads
	private ThreadReadPacket serverReadThread;
	private ThreadWritePacket serverWriteThread;

	// Client Read and Write threads
	private ThreadReadPacket clientReadThread;
	private ThreadWritePacket clientWriteThread;
	
	private PacketHandler packetHandler;
	private Player player;
	
	public ThreadClient(Socket clientSocket) throws UnknownHostException, IOException {
		this.clientSocket = clientSocket;
		this.serverSocket = new Socket("127.0.0.1", 21024);
		
		this.toServerQueue = new PacketQueue();
		this.toClientQueue = new PacketQueue();
		
		this.packetHandler = new PacketHandler(this);
	}
	
	@Override
	public void run() {
		
		try {
			serverStreams.setInputStream(serverSocket.getInputStream());
			serverStreams.setOutputStream(serverSocket.getOutputStream());
			clientStreams.setInputStream(clientSocket.getInputStream());
			clientStreams.setOutputStream(clientSocket.getOutputStream());
			
			// Reading packets from Server InputStream
			serverReadThread = new ThreadReadPacket(this, serverStreams.getInputStream(), false);
			
			// Writing to Client Output Stream
			clientWriteThread = new ThreadWritePacket(toClientQueue, clientStreams.getOutputStream());
			
			// Reading from Client InputStream
			clientReadThread = new ThreadReadPacket(this, clientStreams.getInputStream(), true);
			
			// Writing to Server Output Stream
			serverWriteThread = new ThreadWritePacket(toServerQueue, serverStreams.getOutputStream());
			
			serverReadThread.start();
			serverWriteThread.start();
			clientReadThread.start();
			clientWriteThread.start();
			
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
	
	public PacketQueue getToServerQueue() {
		return toServerQueue;
	}	
	public PacketQueue getToClientQueue() {
		return toClientQueue;
	}
	
	public PacketHandler getPacketHandler() {
		return packetHandler;
	}
	
	public Socket getClientSocket() {
		return clientSocket;
	}
	
	public ServerStreams getServerStreams() {
		return serverStreams;
	}
	public ClientStreams getClientStreams() {
		return clientStreams;
	}
	
	public synchronized void disconnect() {
		Wrapper.getServer().playerList.disconnect(this);
		
		serverReadThread.setRunning(false);
		serverWriteThread.setRunning(false);
		
		clientReadThread.setRunning(false);
		clientWriteThread.setRunning(false);
	}
}