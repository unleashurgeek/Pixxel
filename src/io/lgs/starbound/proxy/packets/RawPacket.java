package io.lgs.starbound.proxy.packets;

import java.io.IOException;

import io.lgs.starbound.util.ByteArrayDataInput;

public class RawPacket {
	
	private ByteArrayDataInput dataInput;
	
	public int type;

	public int data_length;
	public byte[] data;
	public int data_pos;

	public int packet_length = 0;
	public boolean zlib;
	public boolean eop = false;
		
	public RawPacket() {}
}
