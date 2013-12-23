package io.lgs.starbound.proxy.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import io.lgs.starbound.util.ByteArrayDataInput;
import io.lgs.starbound.util.ByteArrayDataOutputStream;
import io.lgs.starbound.util.Compressor;
import io.lgs.starbound.util.Util;

public class RawPacket {
	
	private ByteArrayDataInput dataInput;
	
	public int type;

	public int data_length;
	public byte[] data;
	public int data_pos;

	public int packet_length = 0;
	public boolean zlib;
	public boolean eop = true;
	public boolean isInitialized = false;
	public byte[] buffer;
	
	public RawPacket() {
	}
	
	public void fetchPacket(ByteArrayDataInput dataInput) throws IOException {
		if (this.eop) {
			this.type = dataInput.readVLQ();
			this.data_length = dataInput.readSVLQ();

			if (this.data_length < 0) {
				this.data_length = (-this.data_length - 1);
				this.zlib = true;
			}

			this.data = new byte[this.data_length];
			this.data_pos = dataInput.readBytes(this.data);

			if (this.data_pos == this.data_length) {
				this.eop = true;
				return;
			} else {
				this.eop = false;
			}
		} else if (!this.eop) {
			this.data_pos = dataInput.readBytes(this.data, this.data_pos,
					this.data_length - this.data_pos);

			if (this.data_pos == this.data_length) {
				this.eop = true;
				return;
			}
		}
	}
	
	public void writeRawPacket(DataOutput dataOutput) throws IOException {
		writeRawPacket((ByteArrayDataOutputStream) dataOutput);
	}
	
	public void writeRawPacket(ByteArrayDataOutputStream dataOutput) throws IOException {
		// ok i could also save a raw copy of the buffer, but i want it this way
		ByteBuffer buf = ByteBuffer.allocate(1000000);
		
		buf.put(Util.encodeInt(this.type));
		
		int length = this.data_length;
		
		if (this.zlib) {
			length = -(length + 1);
		}
		
		int test = 0;
		test = buf.limit();
		
		buf.put(Util.encodeSInt(length));
		buf.put(this.data);
		
		dataOutput.writeBytes(Arrays.copyOf( buf.array(), buf.position() ));
		dataOutput.flush();
	}
}
