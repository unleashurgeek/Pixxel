package io.lgs.starbound.proxy.packets;

import java.io.IOException;

import io.lgs.starbound.util.ByteArrayDataInput;

public class RawPacket {
	
	public int type;

    public int data_length;
    public byte[] data;
    public int data_pos;

    public int packet_length = 0;
    public boolean zlib;
    public boolean eop = true;
    
    public RawPacket() {}
    
    public void fetchPacket(ByteArrayDataInput dataInput) throws IOException {
            if (this.eop) {
                    this.type = dataInput.readVLQ();
                    this.data_length = dataInput.readSVLQ();

                    if (this.data_length < 0) {
                            this.data_length = (-this.data_length - 1);
                            this.zlib = true;
                    }
                    
                    System.out.println("ID: " + this.type +  " Size: " + this.data_length);
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
}
