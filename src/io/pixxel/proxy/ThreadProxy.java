package io.pixxel.proxy;

import io.pixxel.Pixxel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

public class ThreadProxy extends Thread {
	
	private ServerSocket serverSocket;
	
	public ThreadProxy() {}
	
	 @Override
	public void run() {
		 try {
			serverSocket = new ServerSocket(21025);
			
			while (serverSocket.isBound() && !serverSocket.isClosed()) {
				
				Socket clientSocket = serverSocket.accept();
				Pixxel.getServer().getLogger().log(Level.INFO, "Proxy connection recieved from " + clientSocket.getInetAddress().getHostAddress());
				Pixxel.getServer().playerList.attemptLogin(clientSocket);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			Pixxel.getServer().getLogger().log(Level.SEVERE, "Could not create serverSocket, crashing server!");
			try {
				kill();
			} catch (IOException e1) {}
		}
	}
	 
	public void kill() throws IOException {
		Pixxel.getServer().kickAll();
		serverSocket.close();
	}
}
