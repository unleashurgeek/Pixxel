package io.lgs.starbound.proxy.packets;

import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.IntHashMap;
import io.lgs.starbound.util.Util;

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

	public static RawPacket fetchRawPacket(ByteArrayDataInput dataInput)
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
	}

	/**
	 * Parse a packet, prefixed by its ID, from the raw packet data.
	 */
	public static Packet parsePacket(RawPacket pkt, boolean isServer)
			throws IOException {
		Packet packet = null;
		int packetID = pkt.type;

		if (isServer && !serverPacketIdList.contains(packetID) || !isServer
				&& !clientPacketIdList.contains(packetID)) {
			return null;
		}

		packet = getNewPacket(packetID);
		if (packet == null)
			return null;

		if (pkt.zlib)
			pkt.data = Util.zlibDecompress(pkt.data);

		packet.readPacketData(new ByteArrayDataInput(pkt.data));

		return packet;
	}
	
	public void writePacket(DataOutput dataOutput) throws IOException {
		Packet packet = this;
		dataOutput.write(packet.getPacketId());
		
		// TODO: Change to Adaptive Variant packetSize
		dataOutput.write(Util.encodeVLQ(packet.getPacketSize()));
		packet.writePacketData(dataOutput);
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
		addIdClassMapping(1, true, false, Packet1ProtocolVersion.class);
		addIdClassMapping(2, true, false, Packet2ConnectResponse.class);
		addIdClassMapping(5, true, false, Packet5ChatReceive.class);
		addIdClassMapping(7, true, false, Packet7ClientConnect.class);
	}
}
