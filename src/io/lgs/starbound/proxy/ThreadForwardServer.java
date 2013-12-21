package io.lgs.starbound.proxy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ThreadForwardServer extends Thread {
	private static final int BUFFER_SIZE = 1460;
	private final DataInputStream input;
	private final DataOutputStream output;
	private final ThreadClient client;
	
	public ThreadForwardServer(ThreadClient client, DataInputStream input, DataOutputStream output) {
		this.client = client;
		this.input = input;
		this.output = output;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[BUFFER_SIZE];
		int bufferSize;
		try {
			while ((bufferSize = input.read(buffer)) != -1) {
				
				//this.outputStream.write(buffer, 0, bufferSize);
				//this.outputStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Something went wrong and broke the forward thread of " + client.getClientSocket().getInetAddress());
		}
		this.client.disconnect();
	}
}
