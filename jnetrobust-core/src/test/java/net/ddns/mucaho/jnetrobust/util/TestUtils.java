package net.ddns.mucaho.jnetrobust.util;

import java.util.Random;

public class TestUtils {

	public final static long nextLong(Random rng, long n) {
		// error checking and 2^x checking removed for simplicity.
		long bits, val;
		do {
			bits = (rng.nextLong() << 1) >>> 1;
			val = bits % n;
		} while (bits-val+(n-1) < 0L);
		return val;
	}
	
	public final static long binLong(String binaryString) {
		return Long.parseLong(binaryString, 2);
	}
	
	public final static int binInt(String binaryString) {
		return Integer.parseInt(binaryString, 2);
	}
}
