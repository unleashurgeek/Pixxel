package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.ByteArrayDataOutputStream;

import java.io.IOException;

public class Packet02ConnectResponse extends Packet {

	/** Unknown */
	public byte unk1;

	/** Unknown */
	public String unk2;

	public Packet02ConnectResponse() {
		// TODO: Read input
	}

	@Override
	public void readPacketData(ByteArrayDataInput dataInput) throws IOException {
		unk1 = dataInput.readByte();
		unk2 = dataInput.readString();

		if (unk2.length() == 0) {
			dataInput.skipBytes(1);
		}
	}

	@Override
	public void writePacketData(ByteArrayDataOutputStream dataOutput) throws IOException {
		// TODO Auto-generated method stub

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
