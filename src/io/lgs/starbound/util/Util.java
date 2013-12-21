package io.lgs.starbound.util;

public class Util {
	// Converts hexString to hexArrayS
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	/**
	 * Converts VLQ Byte[] to Long
	 */
	public static long decodeVLQ(byte[] b) {
		long n = 0;
	    for (int i = 0; i < b.length; i++)
	    {
	      int curByte = b[i] & 0xFF;
	      n = (n << 7) | (curByte & 0x7F);
	      if ((curByte & 0x80) == 0)
	        break;
	    }
	    return n;
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
	    for (int i = numBytes - 1; i >= 0; i--)
	    {
	      int curByte = (int)(n & 0x7F);
	      if (i != (numBytes - 1))
	        curByte |= 0x80;
	      output[i] = (byte)curByte;
	      n >>>= 7;
	    }
	    return output;
	}
}
