package net.ddns.mucaho.jnetrobust.util;

import static org.junit.Assert.*;
import static net.ddns.mucaho.jnetrobust.util.BitConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import net.ddns.mucaho.jnetrobust.util.ShiftableBitSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ShiftableBitSetTest {

	@Parameters
	public static Collection<Object[]> data() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		Random rng = new Random();
		long bitSet = 0L;
		long newBitSet = 0L;
		boolean isSet;
		int index;

		
		newBitSet = bitSet | LSB;
		list.add(new Object[] {bitSet, 0, true, newBitSet});
		bitSet = newBitSet;
		
		newBitSet = bitSet | MSB;
		list.add(new Object[] {bitSet, SIZE - 1, true, newBitSet});
		bitSet = newBitSet;
		
		for (int i=0; i<100; i++) {
			isSet = rng.nextBoolean();
			index = rng.nextInt(SIZE);
			
			newBitSet = isSet ? (bitSet | LSB << index) : (bitSet & ~(LSB << index));
			list.add(new Object[] { bitSet, index, isSet, newBitSet});
			bitSet = newBitSet;
			
			
			
			index = rng.nextInt(SIZE - 1) + 1;
			
			newBitSet = bitSet << index;
			newBitSet = isSet ? (newBitSet | LSB) : (newBitSet & ~LSB);
			list.add(new Object[] { bitSet, -index, isSet, newBitSet});
			bitSet = newBitSet;
			
			
			index += SIZE;
			
			newBitSet = bitSet >>> (index - SIZE + 1);
			newBitSet = isSet ? (newBitSet | MSB) : (newBitSet & ~MSB);
			list.add(new Object[] { bitSet, index, isSet, newBitSet});
			bitSet = newBitSet;
		}

		return list;
	}

	private int index;
	private long expectedBitSet;
	private boolean set;
	private ShiftableBitSet bitSet;
	public ShiftableBitSetTest(long bitSet, int index, boolean set, long expectedBitSet) {
		this.index = index;
		this.expectedBitSet = expectedBitSet;
		this.set = set;
		this.bitSet = new ShiftableBitSet(bitSet);
	}
	
	@Test
	public final void testSetIntBoolean() {
		String debugMsg = ""+bitSet.toString()+".set("+index+", "+set+") != "+
			new ShiftableBitSet(expectedBitSet).toString();
		
		this.bitSet.set(index, set);
		assertEquals(debugMsg, expectedBitSet, this.bitSet.get());
	}

}
