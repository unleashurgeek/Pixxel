package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.ByteArrayDataOutputStream;

import java.io.IOException;

public class Packet12ClientContext extends Packet {

	// Not finished yet, based on my packet

	// 4 unknown bytes, first one is a letter
	public String unk1; // argument
	// 2 unknown bytes
	public String unk2; // player
	// 1 unknown bytes
	public String username; // Seberoth
	public String unk3; // handler
	// 1 unknown bytes
	public String unk4; // team.pollInvitation
	public String unk5; // id
	// 2 unknown bytes
	public String unk6; // command
	// 1 unknown bytes
	public String unk7; // request

	public Packet12ClientContext() {

	}

	@Override
	public void readPacketData(ByteArrayDataInput dataInput) throws IOException {
		// TODO Auto-generated method stub

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
