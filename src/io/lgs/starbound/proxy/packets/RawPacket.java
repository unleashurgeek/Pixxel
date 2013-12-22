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

	//public int round_length = 0;
	
	public RawPacket(ByteArrayDataInput input) throws IOException {
		this.dataInput = input;
		
		this.type = dataInput.readVLQ();
		this.data_length = dataInput.readSVLQ();

		if (this.data_length < 0) {
			this.data_length = (-this.data_length);
			this.zlib = true;
		}

		this.data = new byte[this.data_length];
		this.data_pos = dataInput.readBytes(this.data);

		//this.round_length = dataInput.getPosition();

		if (this.data_pos == this.data_length)
			this.eop = true;
		else
			System.out.println("EOP is false");
	}
	
	
	/*public static RawPacket fetchRawPacket(ByteArrayDataInput dataInput)
	throws IOException {
return fetchRawPacket(dataInput, null);
}

public static RawPacket fetchRawPacket(ByteArrayDataInput dataInput,
	RawPacket pkt) throws IOException {
if (pkt == null) {
	pkt = new RawPacket();
	pkt.type = dataInput.readVLQ();
	pkt.data_length = dataInput.readSVLQ();

	if (pkt.data_length < 0) {
		pkt.data_length = (-pkt.data_length);
		pkt.zlib = true;
	}

	pkt.data = new byte[pkt.data_length];
	pkt.data_pos = dataInput.readBytes(pkt.data);

	pkt.round_length = dataInput.getPosition();

	if (pkt.data_pos == pkt.data_length) {
		pkt.eop = true;
		return pkt;
	}
} else if (!pkt.eop) {
	pkt.data_pos += dataInput.readBytes(pkt.data, pkt.data_pos,
			pkt.data_length - pkt.data_pos);

	pkt.round_length = dataInput.getPosition();

	if (pkt.data_pos == pkt.data_length) {
		pkt.eop = true;
		return pkt;
	} else {
		pkt.eop = false;
	}
}

return null;
}*/
}
