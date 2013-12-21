package io.lgs.starbound.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadProxy extends Thread {
	
	private ServerSocket serverSocket;
	
	public ThreadProxy() { }
	
	 @Override
	public void run() {
		 try {
			serverSocket = new ServerSocket(21024);
			Socket socketServer = new Socket("127.0.0.1", 21024);
			
			while (serverSocket.isBound() && !serverSocket.isClosed()) {
				
				Socket clientSocket = serverSocket.accept();
				
				System.out.println("proxy: Connection recieved from " + clientSocket.getInetAddress().getHostAddress());
				
				// TODO: playerList.attemptConnect(clientSocket);
				ThreadClient client = new ThreadClient(clientSocket, socketServer);
				client.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not create serverSocket!");
		}
	}
	 
	public void kill() throws IOException {
		serverSocket.close();
	}
}
