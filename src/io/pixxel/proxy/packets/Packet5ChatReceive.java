package io.pixxel.proxy.packets;

import io.pixxel.util.ByteArrayDataInput;
import io.pixxel.util.ByteArrayDataOutputStream;
import io.pixxel.util.ChatColor;

import java.io.IOException;

public class Packet5ChatReceive extends Packet {

	/** Color of the Chat. */
	public byte color;

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

	public Packet5ChatReceive(ChatColor color, String sender, String message) {
		this.color  = color.getByte();
		this.sender =  sender;
		this.message = message;
	}
		
	@Override
	public void readPacketData(ByteArrayDataInput dataInput) throws IOException {
		color = dataInput.readByte();
		channel = dataInput.readString();
		receiver = dataInput.readString();
		connection_number = dataInput.readVLQ();
		sender = dataInput.readString();
		message = dataInput.readString();
	}

	@Override
	public void writePacketData(ByteArrayDataOutputStream output) throws IOException {
		output.writeByte(color);
		output.writeString(channel);
		output.writeString(receiver);
		output.writeVLQ(connection_number);
		output.writeString(sender);
		output.writeString(message);
	}

	@Override
	public void processPacket(PacketHandler packetHandler) {
		// packetHandler.handleChatSend(this);
	}

	@Override
	public int getPacketSize() {
		int size = 0;
		if (channel.length() == 0)
			size += 2;
		else
			size += channel.length() + 1;
		
		if (receiver.length() == 0)
			size +=2;
		else
			size += receiver.length() + 1;
		
		if (sender.length() == 0)
			size += 2;
		else
			size += sender.length() +1;
		
		if (message.length() == 0)
			size += 2;
		else
			size += message.length() + 1;
		
		// Chat Color
		size += 1;
		
		return size;
	}

}
