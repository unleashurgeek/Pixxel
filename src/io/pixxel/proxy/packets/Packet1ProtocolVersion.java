package io.pixxel.proxy.packets;

import io.pixxel.util.ByteArrayDataInput;
import io.pixxel.util.ByteArrayDataOutputStream;

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
	public void writePacketData(ByteArrayDataOutputStream dataOutput) throws IOException {
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
