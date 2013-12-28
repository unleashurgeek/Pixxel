package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.ByteArrayDataOutputStream;
import io.lgs.starbound.util.Compressor;
import io.lgs.starbound.util.IntHashMap;
import io.lgs.starbound.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Packet {
	
	/** Maps packet id to packet class */
	public static IntHashMap packetIdToClassMap = new IntHashMap();
	
	/** Maps packet class to packet id */
	@SuppressWarnings("rawtypes")
	private static Map<Class, Integer> packetClassToIdMap = new HashMap<Class, Integer>();
	
	/** List of the client's packet IDs. */
	private static Set<Integer> clientPacketIdList = new HashSet<Integer>();
	
	/** List of the server's packet IDs */
	private static Set<Integer> serverPacketIdList = new HashSet<Integer>();
	
	private boolean isCompressed = false;
	
	/**
	 * Adds a two way mapping between the packet ID and packet class.
	 */
	@SuppressWarnings("rawtypes")
	public static void addIdClassMapping(int packetID, boolean isToClientPacket, boolean isToServerPacket, Class packetClass) {
		if (packetIdToClassMap.containsItem(packetID)) {
			throw new IllegalArgumentException("Duplicated packet id: " + packetID);
		} else if (packetClassToIdMap.containsKey(packetClass)) {
			throw new IllegalArgumentException("Duplicate packet class: " + packetClass);
		} else {
			packetIdToClassMap.addKey(packetID, packetClass);
			packetClassToIdMap.put(packetClass, packetID);
			
			if (isToClientPacket)
				clientPacketIdList.add(packetID);
			
			if (isToServerPacket)
				serverPacketIdList.add(packetID);
		}
	}
	
	/**
	 * Returns a new instance of the specified Packet Class.
	 */
	@SuppressWarnings("unchecked")
	public static Packet getNewPacket(int packetID) {
		try {
			Class<Packet> pClass = (Class<Packet>)packetIdToClassMap.lookup(packetID);
			return pClass == null ? null : (Packet)pClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the ID of this packet.
	 */
	public final int getPacketId() {
		return packetClassToIdMap.get(this.getClass()) == null ? 0 : (Integer)packetClassToIdMap.get(this.getClass()).intValue();
	}

	/**
	 * Parse a packet, prefixed by its ID, from the raw packet data.
	 */
	public static Packet readPacket(RawPacket rawPacket, boolean isToServer) throws IOException {
		Packet packet = null;
		int packetID = rawPacket.type;
		ByteArrayDataInput byteArrayDataInput;
		
		if (isToServer && !serverPacketIdList.contains(packetID) || !isToServer
				&& !clientPacketIdList.contains(packetID)) {
			packet = new Packet0Generic(isToServer);
			
			((Packet0Generic)packet).type 	= rawPacket.type;
			((Packet0Generic)packet).size = rawPacket.data_length;
			((Packet0Generic)packet).data	= rawPacket.data;
			((Packet0Generic)packet).isCompressed = rawPacket.zlib;
			
			return packet;
		}
		
		packet = getNewPacket(packetID);
		if (packet == null) {
			packet = new Packet0Generic(isToServer);
			((Packet0Generic)packet).type 	= rawPacket.type;
			((Packet0Generic)packet).size = rawPacket.data_length;
			((Packet0Generic)packet).data	= rawPacket.data;
			((Packet0Generic)packet).isCompressed = rawPacket.zlib;
			
			return packet;
		}
		
		byte[] buff = rawPacket.data;
		
		if (rawPacket.zlib) {
			buff = Compressor.decompress(rawPacket.data);
		}
		
		byteArrayDataInput = new ByteArrayDataInput(buff);
		packet.readPacketData(byteArrayDataInput);
		
		return packet;
	}
	
	public void writePacket(DataOutput dataOutput) throws IOException {
		Packet.writePacket(this, (ByteArrayDataOutputStream) dataOutput);
	}
	
	/**
	 * Writes a packet, prefixed by its ID, to the data stream.
	 */
	public static void writePacket(Packet packet, ByteArrayDataOutputStream dataOutput) throws IOException {
		if (packet.getPacketId() == 0) {
			if (((Packet0Generic)packet).isCompressed) {
				dataOutput.writeVLQ(((Packet0Generic)packet).type);
				dataOutput.writeSVLQ(-((Packet0Generic)packet).size + 2);
				dataOutput.writeBytes(((Packet0Generic)packet).data);
				dataOutput.flush();
			} else {
				dataOutput.writeVLQ(((Packet0Generic)packet).type);
				dataOutput.writeSVLQ(((Packet0Generic)packet).size);
				dataOutput.writeBytes(((Packet0Generic)packet).data);
				dataOutput.flush();
			}
			return;
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteArrayDataOutputStream bados = new ByteArrayDataOutputStream(baos);
		packet.writePacketData(bados);
		System.out.println("Wrote Packet Data");
		//bados.flush();
		byte[] data = baos.toByteArray();
		bados.close();
		
		if (packet.getPacketSize() > 255) {
			data = Compressor.compress(data);
			System.out.println("Packet compressed");
			packet.isCompressed = true;
		}
		
		dataOutput.writeVLQ(packet.getPacketId());
		if (packet.isCompressed)
			dataOutput.writeSVLQ(-packet.getPacketSize());
		else
			dataOutput.writeSVLQ(packet.getPacketSize());
		dataOutput.writeBytes(data);
		dataOutput.flush();
		System.out.println("Packet Flushed");
	}
	
	/**
	 * Abstract. Reads the raw packet data from the data stream.
	 */
	public abstract void readPacketData(ByteArrayDataInput dataInput) throws IOException;
	
	/**
	 * Abstract. Writes the raw packet data to the data stream.
	 */
	public abstract void writePacketData(ByteArrayDataOutputStream dataOutput) throws IOException;
	
	/**
	 * Abstract. Passes this packet on to the PacketHandler for processing.
	 */
	public abstract void processPacket(PacketHandler packetHandler);
	
	/**
	 * Abstract. Return the size of packet (not counting the header and VLQ).
	 */
	public abstract int getPacketSize();
	
	/**
	 * Only false for the abstract Packet class, all real packets return true.
	 */
	public boolean isRealPacket() {
		return false;
	}
	
	static {
		// TODO: Uncomment. Commented for testing purposes.
		//addIdClassMapping(1, true, false, Packet1ProtocolVersion.class);
		//addIdClassMapping(2, true, false, Packet2ConnectResponse.class);
		//addIdClassMapping(5, true, false, Packet5ChatReceive.class);
		//addIdClassMapping(7, false, true, Packet7ClientConnect.class);
		//addIdClassMapping(11, false, true, Packet11ChatSend.class);
	}
}
