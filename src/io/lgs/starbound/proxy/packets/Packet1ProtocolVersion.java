package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;

import java.io.DataOutput;
import java.io.IOException;

public class Packet1ProtocolVersion extends Packet {

	/** Version of the server. */
	public int version;

	public Packet1ProtocolVersion() {
		// TODO: Read input
	}

	@Override
	public void readPacketData(ByteArrayDataInput dataInput) throws IOException {
		version = dataInput.readInt();
	}

	@Override
	public void writePacketData(DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(version);
	}

	@Override
	public void processPacket(PacketHandler packetHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPacketSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
