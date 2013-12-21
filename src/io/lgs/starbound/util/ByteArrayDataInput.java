package io.lgs.starbound.util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

// TODO: Hope this is ok, if not...

public class ByteArrayDataInput implements DataInput {

	private final byte[] data;
	private final ByteBuffer buffer;

	public ByteArrayDataInput(byte[] src) {
		if (src == null) {
			throw new NullPointerException();
		}
		data = src;
		buffer = ByteBuffer.wrap(data);
	}

	public int readVLQ() throws IOException {
		int n = 0;
		int curByte;

		do {
			byte b = this.readByte();
			curByte = b & 0xFF;
			n = (n << 7) | (curByte & 0x7F);
		} while ((curByte & 0x80) != 0);

		return n;
	}

	public int readSVLQ() throws IOException {
		return Util.decodeZigZag32(readVLQ());
	}

	public int readBytes(byte[] b) {
		return readBytes(b, 0, b.length);
	}

	public int readBytes(byte[] b, int off, int len) {
		int i = off;

		try {
			for (i = off; i < off + len; i++) {
				b[i] = buffer.get();
			}
		} catch (BufferUnderflowException e) {
			return i;
		}

		return i;
	}

	public int getPosition() {
		return buffer.position();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[])
	 */
	@Override
	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		try {
			buffer.get(b, 0, len);
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#skipBytes(int)
	 */
	@Override
	public int skipBytes(int n) throws EOFException {
		buffer.position(buffer.position() + n);
		return n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readBoolean()
	 */
	@Override
	public boolean readBoolean() throws EOFException {
		try {
			int value = buffer.get();
			if (value > 0)
				return true;
			else
				return false;
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readByte()
	 */
	@Override
	public byte readByte() throws EOFException {
		try {
			return buffer.get();
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	@Override
	public int readUnsignedByte() throws EOFException {
		try {
			int value = buffer.get();
			return value & 0xFF;
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readShort()
	 */
	@Override
	public short readShort() throws EOFException {
		try {
			return buffer.getShort();
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	@Override
	public int readUnsignedShort() throws EOFException {
		try {
			int value = buffer.getShort();
			return value & 0xFFFF;
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readChar()
	 */
	@Override
	public char readChar() throws EOFException {
		try {
			return buffer.getChar();
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readInt()
	 */
	@Override
	public int readInt() throws EOFException {
		try {
			return buffer.getInt();
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readLong()
	 */
	@Override
	public long readLong() throws EOFException {
		try {
			return buffer.getLong();
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFloat()
	 */
	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readDouble()
	 */
	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUTF()
	 */
	@Override
	public String readUTF() throws IOException {
		return DataInputStream.readUTF(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readLine()
	 */
	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}
}
