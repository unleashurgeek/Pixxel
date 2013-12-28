package io.lgs.starbound.proxy;

import io.lgs.starbound.Pixxel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadProxy extends Thread {
	
	private ServerSocket serverSocket;
	
	public ThreadProxy() {}
	
	 @Override
	public void run() {
		 try {
			serverSocket = new ServerSocket(21025);
			
			while (serverSocket.isBound() && !serverSocket.isClosed()) {
				
				Socket clientSocket = serverSocket.accept();
				System.out.println("proxy: Connection recieved from " + clientSocket.getInetAddress().getHostAddress());
				Pixxel.getServer().playerList.attemptLogin(clientSocket);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not create serverSocket!");
		}
	}
	 
	public void kill() throws IOException {
		Pixxel.getServer().kickAll();
		serverSocket.close();
	}
}
