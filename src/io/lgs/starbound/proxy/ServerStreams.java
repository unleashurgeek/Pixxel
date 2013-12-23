package io.lgs.starbound.proxy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ServerStreams {
	private DataInputStream  inputStream;
	private DataOutputStream outputStream;
	
	public ServerStreams() {};
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = new DataInputStream(inputStream);
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = new DataOutputStream(outputStream);
	}
	
	public DataInputStream getInputStream() {
		return inputStream;
	}
	
	public DataOutputStream getOutputStream() {
		return outputStream;
	}
}
