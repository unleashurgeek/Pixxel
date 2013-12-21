package io.lgs.starbound.proxy.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.lgs.starbound.util.IntHashMap;
import io.lgs.starbound.util.Util;

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
	public static void addIdClassMapping(int packetID, boolean isClientPacket, boolean isServerPacket, Class packetClass) {
		if (packetIdToClassMap.containsItem(packetID)) {
			throw new IllegalArgumentException("Duplicated packet id: " + packetID);
		} else if (packetClassToIdMap.containsKey(packetClass)) {
			throw new IllegalArgumentException("Duplicate packet class: " + packetClass);
		} else {
			packetIdToClassMap.addKey(packetID, packetClass);
			packetClassToIdMap.put(packetClass, packetID);
			
			if (isClientPacket)
				clientPacketIdList.add(packetID);
			
			if (isServerPacket)
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
	 * Read a packet, prefixed by its ID, from the data stream.
	 */
	public static Packet readPacket(DataInput dataInput, boolean isServer) throws IOException {
		Packet packet = null;
		int packetID;
		try {
			packetID = dataInput.readUnsignedByte();
			// TODO: Rewrite VLQ to Variant with adaptive num size.
			// TODO: Read VLQ. Need to determine VLQ size.
			// TODO: Detect and decrypt zlib compression.
			
			if (isServer && !serverPacketIdList.contains(packetID) || !isServer && !clientPacketIdList.contains(packetID)) {
				return null;
			}
			
			packet = getNewPacket(packetID);
			if (packet == null)
				return null;
			
			packet.readPacketData(dataInput);
		} catch (EOFException e) {
			e.printStackTrace();
		}
		
		return packet;
	}
	
	/**
	 * Writes a packet, prefixed by its ID, to the data stream.
	 */
	public static void writePacket(Packet packet, DataOutput dataOutput) throws IOException {
		dataOutput.write(packet.getPacketId());
		
		// TODO: Change to Adaptive Variant packetSize
		dataOutput.write(Util.encodeVLQ(packet.getPacketSize()));
		packet.writePacketData(dataOutput);
	}
	
	/**
	 * Abstract. Reads the raw packet data from the data stream.
	 */
	public abstract void readPacketData(DataInput dataInput) throws IOException;
	
	/**
	 * Abstract. Writes the raw packet data to the data stream.
	 */
	public abstract void writePacketData(DataOutput dataOutput) throws IOException;
	
	/**
	 * Abstract. Passes this packet on to the PacketHandler for processing.
	 */
	public abstract void processPacket(PacketHandler packetHandler);
	
	/**
	 * Abstract. Return the size of (not counting the header and VLQ).
	 */
	public abstract int getPacketSize();
	
	/**
	 * Only false for the abstract Packet class, all real packets return true.
	 */
	public boolean isRealPacket() {
		return false;
	}
	
	static {
		addIdClassMapping(5, true, false, Packet5ChatReceive.class);
	}
}
