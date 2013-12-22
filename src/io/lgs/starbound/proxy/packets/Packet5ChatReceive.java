package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;

import java.io.DataOutput;
import java.io.IOException;

public class Packet5ChatReceive extends Packet {

	/** Color of the Chat. */
	public int color;

	/** Channel Name */
	public String channel;

	/** Receiver name */
	public String receiver;

	/** Connection # of sender */
	public int connection_number;

	/** Sender name */
	public String sender;

	/** Message */
	public String message;

	public Packet5ChatReceive() {
		// TOOO: Read input
	}

	@Override
	public void readPacketData(ByteArrayDataInput dataInput) throws IOException {
		color = dataInput.readVLQ();
		channel = dataInput.readString();

		if (channel.length() == 0) {
			dataInput.skipBytes(1);
		}

		receiver = dataInput.readString();

		if (receiver.length() == 0) {
			dataInput.skipBytes(1);
		}

		connection_number = dataInput.readVLQ();
		sender = dataInput.readString();

		if (sender.length() == 0) {
			dataInput.skipBytes(1);
		}

		message = dataInput.readString();

		if (message.length() == 0) {
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
		return 0;
	}

}
