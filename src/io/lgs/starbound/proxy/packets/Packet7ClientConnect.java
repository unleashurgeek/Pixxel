package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.ByteArrayDataOutputStream;

import java.io.IOException;

public class Packet7ClientConnect extends Packet {

	public int unk1_length;
	public byte[] unk1;
	public int unk2_length; // not sure about this was 0101 could be fixed
	public byte[] unk2; // not sure about this
	public byte[] uuid;
	public String username;
	public String race;
	public byte[] playerdata; // Ship, Player (unknwon length, could be
								// calculated, ignore it at the moment)

	public Packet7ClientConnect() {

	}

	@Override
	public void readPacketData(ByteArrayDataInput dataInput) throws IOException {
		unk1_length = dataInput.readVLQ();

		unk1 = new byte[unk1_length];
		dataInput.readFully(unk1);

		unk2_length = dataInput.readVLQ();

		unk2 = new byte[unk2_length];
		dataInput.readFully(unk2);

		uuid = new byte[16];
		dataInput.readFully(uuid);

		username = dataInput.readString();

		race = dataInput.readString();
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
