package io.lgs.starbound.proxy;

import io.lgs.starbound.Wrapper;

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
			
			while (serverSocket.isBound() && !serverSocket.isClosed()) {
				
				Socket clientSocket = serverSocket.accept();
				if (!clientSocket.getInetAddress().getHostAddress().equals("127.0.0.1")) {
					System.out.println("proxy: Connection recieved from " + clientSocket.getInetAddress().getHostAddress());
					Wrapper.getServer().playerList.attemptLogin(clientSocket);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not create serverSocket!");
		}
	}
	 
	public void kill() throws IOException {
		Wrapper.getServer().kickAll();
		serverSocket.close();
	}
}
