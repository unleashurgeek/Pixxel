package io.lgs.starbound.proxy;

import io.lgs.starbound.util.ByteArrayDataOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ServerStreams {
	private DataInputStream  inputStream;
	private ByteArrayDataOutputStream outputStream;
	
	public ServerStreams() {};
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = new DataInputStream(inputStream);
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = new ByteArrayDataOutputStream(outputStream);
	}
	
	public DataInputStream getInputStream() {
		return inputStream;
	}
	
	public ByteArrayDataOutputStream getOutputStream() {
		return outputStream;
	}
}
