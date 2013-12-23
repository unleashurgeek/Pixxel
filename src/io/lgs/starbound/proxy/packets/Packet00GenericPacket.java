package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.ByteArrayDataOutputStream;

import java.io.IOException;

public class Packet00GenericPacket extends Packet {
	
	public int type;
	public int length;
	public byte[] data;
	
	public Packet00GenericPacket() {
	}
	
	@Override
	public void readPacketData(ByteArrayDataInput dataInput) throws IOException {
	}

	@Override
	public void writePacketData(ByteArrayDataOutputStream dataOutput)
			throws IOException {
		dataOutput.writeBytes(data);
	}

	@Override
	public void processPacket(PacketHandler packetHandler) {
	}

	@Override
	public int getPacketSize() {
		return 0;
	}
}
