package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.ByteArrayDataOutputStream;

import java.io.IOException;

public class Packet0Generic extends Packet {
	
	public int type;
	public int length;
	public byte[] data;
	
	public boolean isCompressed;
	
	public boolean isToServer;
	
	public Packet0Generic(boolean isToServer) { 
		this.isToServer = isToServer;
	}
	
	@Override
	public void readPacketData(ByteArrayDataInput dataInput) throws IOException {}

	@Override
	public void writePacketData(ByteArrayDataOutputStream dataOutput) throws IOException {}

	@Override
	public void processPacket(PacketHandler packetHandler) {
		packetHandler.handleGeneric(this);
	}

	@Override
	public int getPacketSize() {
		return -1;
	}
}
