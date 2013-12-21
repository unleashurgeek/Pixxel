package io.lgs.starbound.proxy;

import io.lgs.starbound.proxy.packets.Packet;
import io.lgs.starbound.proxy.packets.RawPacket;
import io.lgs.starbound.util.ByteArrayDataInput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ThreadForwardClient extends Thread {
	private static final int BUFFER_SIZE = 1460;
	private final DataInputStream input;
	private final DataOutputStream output;
	private final ThreadClient client;

	public ThreadForwardClient(ThreadClient client, DataInputStream input,
			DataOutputStream output) {
		this.client = client;
		this.input = input;
		this.output = output;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[BUFFER_SIZE];
		RawPacket pkt = new RawPacket();
		int bufferSize;
		try {
			while ((bufferSize = input.read(buffer)) != -1) {

				while (pkt.round_length < bufferSize) {
					if (pkt.eop) {
						pkt = Packet.fetchRawPacket(new ByteArrayDataInput(
								buffer));
					} else {
						pkt = Packet.fetchRawPacket(new ByteArrayDataInput(
								buffer), pkt);
					}

					if (!pkt.eop)
						break;

					// TODO: Handle Packet

				}
				/*
				 * packet = Packet.readPacket(new ByteArrayDataInput(buffer),
				 * false); if (packet != null) { // TODO: Send reconized packets
				 * to Packet Handler to be // reconstructed.
				 * packet.processPacket(client.packetHandler); } else {
				 * this.output.write(buffer, 0, bufferSize); }
				 * 
				 * this.output.flush();
				 */
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Something went wrong and broke the forward thread of "
							+ client.getClientSocket().getInetAddress());
		}
		this.client.disconnect();
	}
}
