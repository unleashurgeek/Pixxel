package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.ByteArrayDataOutputStream;
import io.lgs.starbound.util.Compressor;
import io.lgs.starbound.util.IntHashMap;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
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
		return ((Integer)packetClassToIdMap.get(this.getClass())).intValue();
	}

	/**
	 * Parse a packet, prefixed by its ID, from the raw packet data.
	 */
	public static Packet readPacket(DataInput dataInput, boolean isServer) throws IOException {
		Packet packet = null;
		RawPacket rawPacket = new RawPacket((ByteArrayDataInput) dataInput);
		int packetID = rawPacket.type;
		
		if (isServer && !serverPacketIdList.contains(packetID) || !isServer
				&& !clientPacketIdList.contains(packetID)) {
			return null;
		}
		
		packet = getNewPacket(packetID);
		if (packet == null)
			return null;
		
		if (rawPacket.zlib) {
			rawPacket.data = Compressor.decompress(rawPacket.data);
		}
		
		packet.readPacketData((ByteArrayDataInput) dataInput);
		
		return packet;
	}
	
	public void writePacket(DataOutput dataOutput) throws IOException {
		Packet.writePacket(this, (ByteArrayDataOutputStream) dataOutput);
	}
	
	/**
	 * Writes a packet, prefixed by its ID, to the data stream.
	 */
	public static void writePacket(Packet packet, ByteArrayDataOutputStream dataOutput) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteArrayDataOutputStream bados = new ByteArrayDataOutputStream(baos);
		packet.writePacket(bados);
		bados.flush();
		byte[] data = baos.toByteArray();
		bados.close();
		
		if (packet.getPacketSize() >= 253) {
			data = Compressor.compress(data);
		}
		
		dataOutput.writeVLQ(packet.getPacketId());
		dataOutput.writeSVLQ(packet.getPacketSize());
		dataOutput.writeBytes(data);
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
		addIdClassMapping(1, true, false, Packet1ProtocolVersion.class);
		addIdClassMapping(2, true, false, Packet2ConnectResponse.class);
		addIdClassMapping(5, true, false, Packet5ChatSend.class);
		addIdClassMapping(7, true, false, Packet7ClientConnect.class);
	}
}
