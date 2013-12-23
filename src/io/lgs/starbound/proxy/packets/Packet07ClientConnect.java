package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.ByteArrayDataOutputStream;
import io.lgs.starbound.util.Util;

import java.io.IOException;

public class Packet07ClientConnect extends Packet {
	
	public byte[] data;
	
	public int unk1_length;
	public byte[] unk1; // i really dont know what this bytearray is for
	public int claim_length;
	public byte[] claim; // something todo with attemptAuth config value / ValidClaim
	public String uuid; // i think string is better then byte[]
	public String username;
	public String race;
	public byte[] playerdata; // Ship, Player (unknwon length, could be
								// calculated, ignore it at the moment)

	public Packet07ClientConnect() {}

	@Override
	public void readPacketData(ByteArrayDataInput dataInput) throws IOException {
		data = dataInput.getData();
		
		unk1_length = dataInput.readVLQ();
		unk1 = new byte[unk1_length];
		dataInput.readFully(unk1);

		claim_length = dataInput.readVLQ();

		claim = new byte[claim_length];
		dataInput.readFully(claim);

		byte[] t_uuid = new byte[16];
		dataInput.readFully(t_uuid);
		uuid = Util.bytesToHex(t_uuid);

		username = dataInput.readString();

		race = dataInput.readString();
	}

	@Override
	public void writePacketData(ByteArrayDataOutputStream dataOutput) throws IOException {
		dataOutput.write(data);
	}

	@Override
	public void processPacket(PacketHandler packetHandler) {
		// This packet never should be processed, only read.
	}

	@Override
	public int getPacketSize() {
		return data.length;
	}

}
