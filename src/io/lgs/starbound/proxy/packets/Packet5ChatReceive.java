package io.lgs.starbound.proxy.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Packet5ChatReceive extends Packet {

	/** Color of the Chat. */
	public byte color;
	
	/** Length of channel name */
	public byte channel_length;
	
	/** Channel Name */
	public String channel;
	
	/** Length of Receiver  */
	public byte reciever_length;

	/** Receiver name */
	public String receiver; 
	
	/** Connection # of sender */
	public byte connection_number;
	
	/** Sender name length */
	public byte sender_length;
	
	/** Sender name */
	public String sender;
	
	/** Message length */
	public byte message_length;
	
	/** Message */
	public String message;
	
	public Packet5ChatReceive() {
		//TOOO: Read input
	}
	
	@Override
	public void readPacketData(DataInput dataInput) throws IOException {
		// TODO Auto-generated method stub
		
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
		int base = 0;
		if (channel_length == (byte)0x00)
			base++;
		if (reciever_length == (byte)0x00)
			base++;
		if (sender_length == (byte)0x00)
			base++;
		if (message_length == (byte)0x00)
			base++;
		
		return base + 6 + channel_length + reciever_length + sender_length + message_length;
	}

}
