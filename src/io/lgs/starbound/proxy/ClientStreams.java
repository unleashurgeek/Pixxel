package io.lgs.starbound.proxy;

import io.lgs.starbound.util.ByteArrayDataOutputStream;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ClientStreams {
	private DataInputStream  inputStream;
	private ByteArrayDataOutputStream outputStream;
	
	public ClientStreams() {};
	
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
