package io.lgs.starbound.util;

import java.io.IOException;
import java.util.Arrays;

public class Util {
	public final static int BUFFER_SIZE = 1460;
	
	// Converts hexString to hexArrayS
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static int decodeZigZag32(final int n) {
		return (n >>> 1) ^ -(n & 1);
	}

	public static long decodeZigZag64(final long n) {
		return (n >>> 1) ^ -(n & 1);
	}

	public static int decodeInt(byte[] b) throws IOException {
		return (int) decodeVLQ(b);
	}

	public static int decodeSInt(byte[] b) throws IOException {
		return (int) decodeZigZag64(decodeVLQ(b));
	}

	/**
	 * Converts VLQ Byte[] to Long
	 */
	public static long decodeVLQ(byte[] b) {
		long n = 0;
		for (int i = 0; i < b.length; i++) {
			int curByte = b[i] & 0xFF;
			n = (n << 7) | (curByte & 0x7F);
			if ((curByte & 0x80) == 0)
				break;
		}
		return n;
	}

	public static int encodeZigZag32(final int n) {
		return (n << 1) ^ (n >> 31);
	}

	public static long encodeZigZag64(final long n) {
		return (n << 1) ^ (n >> 63);
	}

	public static byte[] encodeInt(int value) throws IOException {
		return encodeVLQ(value);
	}

	public static byte[] encodeSInt(int value) throws IOException {
		return encodeVLQ(encodeZigZag32(value));
	}
	
	 public static byte[][] divideArray(byte[] source, int chunksize) {
	        byte[][] ret = new byte[(int)Math.ceil(source.length / (double)chunksize)][chunksize];

	        int start = 0;

	        for(int i = 0; i < ret.length; i++) {
	            ret[i] = Arrays.copyOfRange(source,start, start + chunksize);
	            start += chunksize ;
	        }

	        return ret;
	    }

	/**
	 * Converts long to VLQ byte[]
	 */
	public static byte[] encodeVLQ(long n) {
		int numRelevantBits = 64 - Long.numberOfLeadingZeros(n);
		int numBytes = (numRelevantBits + 6) / 7;
		if (numBytes == 0)
			numBytes = 1;
		byte[] output = new byte[numBytes];
		for (int i = numBytes - 1; i >= 0; i--) {
			int curByte = (int) (n & 0x7F);
			if (i != (numBytes - 1))
				curByte |= 0x80;
			output[i] = (byte) curByte;
			n >>>= 7;
		}
		return output;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
