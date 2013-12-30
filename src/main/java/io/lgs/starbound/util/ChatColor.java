package io.lgs.starbound.util;

public enum ChatColor {
	GREEN((byte)0x00),
	DEFAULT((byte)0x01),
	YELLOW((byte)0x01),
	GREY((byte)0x02),
	GRAY((byte)0x03),
	WHITE((byte)0x04);
	
	private byte code;
	private ChatColor(byte code) {
		this.code = code;
	}
	
	public byte getByte() {
		return code;
	}
}
