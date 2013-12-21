package io.lgs.starbound.proxy.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Packet2ConnectResponse extends Packet {

	/** Unknown */
	public byte unk1;

	/** Unknown */
	public String unk2;

	public Packet2ConnectResponse() {
		// TODO: Read input
	}

	@Override
	public void readPacketData(DataInput dataInput) throws IOException {
		unk1 = dataInput.readByte();
		// TODO: Create custom DataInput Class, would be easier then using utils
		// string = (length)readVLQ + message
		// unk2 = dataInput.readString();

		if (unk2.length() == 0) {
			dataInput.skipBytes(1);
		}
	}

	@Override
	public void writePacketData(DataOutput dataOutput) throws IOException {
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
