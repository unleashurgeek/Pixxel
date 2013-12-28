package io.pixxel.proxy.packets;

import io.pixxel.util.ByteArrayDataInput;
import io.pixxel.util.ByteArrayDataOutputStream;

import java.io.IOException;

public class Packet11ChatSend extends Packet {

	/** Message */
	public String message;
	
	/** Channel Number */
	public int channel;
	
	public Packet11ChatSend(String message) {
		this.message = message;
	}
	
	@Override
	public void readPacketData(ByteArrayDataInput dataInput) throws IOException {
		message = dataInput.readString();
		channel = dataInput.readVLQ();
	}

	@Override
	public void writePacketData(ByteArrayDataOutputStream dataOutput)
			throws IOException {
		dataOutput.writeString(message);
		dataOutput.writeVLQ(channel);
	}

	@Override
	public void processPacket(PacketHandler packetHandler) {
		packetHandler.handleChatSend(this);
	}

	@Override
	public int getPacketSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
