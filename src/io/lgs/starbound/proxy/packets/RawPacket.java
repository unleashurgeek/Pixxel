package io.lgs.starbound.proxy.packets;

public class RawPacket {
	public int type;

	public int data_length;
	public byte[] data;
	public int data_pos;

	public int packet_length = 0;
	public boolean zlib;
	public boolean eop = false;

	// TODO:
	public int round_length = 0;

	public RawPacket() {

	}
}
