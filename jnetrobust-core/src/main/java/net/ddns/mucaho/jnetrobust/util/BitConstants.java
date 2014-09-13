package net.ddns.mucaho.jnetrobust.util;

public final class BitConstants {
	public final static int OFFSET = 1;
	public final static int SIZE = Long.SIZE;
	public final static long MSB = 0x8000000000000000L;
	public final static long LSB = 0x1;
	
	private final static long LONG_MASK = 0x7FFFFFFFFFFFFFFFL;
	
	public final static long convertBits(int bits) {
		return ((long)bits) & LONG_MASK;
	}
	
	public final static int convertBits(long bits) {
		return (int)bits;
	}
}
