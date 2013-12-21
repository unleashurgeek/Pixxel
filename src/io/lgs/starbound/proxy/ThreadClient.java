package io.lgs.starbound.proxy;

import java.io.IOException;
import java.net.Socket;

public class ThreadClient extends Thread {
	
	private final Socket clientSocket;
	private final Socket serverSocket;
	private ServerStreams serverStreams;
	private ClientStreams clientStreams;
	
	public ThreadClient(Socket clientSocket, Socket serverSocket) {
		this.clientSocket = clientSocket;
		this.serverSocket = serverSocket;
	}
	
	@Override
	public void run() {
		
		try {
			serverStreams.setInputStream(serverSocket.getInputStream());
			serverStreams.setOutputStream(serverSocket.getOutputStream());
			
			clientStreams.setInputStream(clientSocket.getInputStream());
			clientStreams.setOutputStream(clientSocket.getOutputStream());
			
			// TODO: merge ThreadForwardServer and ThreadForwardClient to ThreadForward
			// Packets Client to Server
			ThreadForwardServer forwardServer = new ThreadForwardServer(this, clientStreams.getInputStream(), serverStreams.getOutputStream());
			
			// Packets Server to Client
			ThreadForwardClient forwardClient =  new ThreadForwardClient(this, serverStreams.getInputStream(), clientStreams.getOutputStream());
			
			// Start Forwards
			forwardServer.start();
			forwardClient.start();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not create Input and Output streams!");
		}
		
		
	}
	
	// TODO: Create packet handler
	
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
	
	public synchronized void disconnect() {
		try {
			this.serverSocket.close();
			this.clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}