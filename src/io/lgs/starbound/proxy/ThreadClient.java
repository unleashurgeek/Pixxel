package io.lgs.starbound.proxy;

import io.lgs.starbound.Wrapper;
import io.lgs.starbound.entity.Player;
import io.lgs.starbound.proxy.packets.Packet;
import io.lgs.starbound.proxy.packets.PacketHandler;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadClient extends Thread {
	
	private final Socket clientSocket;
	private final Socket serverSocket;
	private ServerStreams serverStreams = new ServerStreams();
	private ClientStreams clientStreams = new ClientStreams();
	private PacketHandler packetHandler;
	
	private ThreadForward forwardServer;
	private ThreadForward forwardClient;
	
	
	// TODO: Add a queue system
	public BlockingQueue<Packet> sendToClientQueue = new LinkedBlockingQueue<Packet>();
	public BlockingQueue<Packet> sendToServerQueue = new LinkedBlockingQueue<Packet>();
	
	public BlockingQueue<Packet> receiveFromClientQueue = new LinkedBlockingQueue<Packet>();
	public BlockingQueue<Packet> receiveFromServerQueue = new LinkedBlockingQueue<Packet>();
	
	private Player player;
	
	public ThreadClient(Socket clientSocket) throws UnknownHostException, IOException {
		this.clientSocket = clientSocket;
		this.serverSocket = new Socket("127.0.0.1", 21024);
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
	
	public Packet take(BlockingQueue<Packet> bq) {
        try {
            return bq.take();
        } catch(InterruptedException ie) {
            return null;
        }
    }
	
	public void disconnect() {
		Wrapper.getServer().playerList.disconnect(this);
		forwardServer.setRunning(false);
		forwardClient.setRunning(false);
	}
}