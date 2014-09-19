package net.ddns.mucaho.jnetrobust.util;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class FastLogTest {
    @Test
    @Parameters
    public final void testLog2Long(int bitIndex, long longMSB) {
        Assert.assertEquals("log2(" + longMSB + ") != " + bitIndex,
                bitIndex, FastLog.log2(longMSB));
    }

    public Collection<Object[]> parametersForTestLog2Long() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        Random rand = new Random();

        long longMSB;
        for (int i = 0; i < Long.SIZE; ++i) {
            longMSB = (long) Math.pow(2, i);
            longMSB += TestUtils.nextLong(rand, longMSB);
            list.add(new Object[]{i, longMSB});
        }

        return list;
    }


    @Test
    @Parameters
    public final void testLog2Int(int bitIndex, int intMSB) {
        assertEquals("log2(" + intMSB + ") != " + bitIndex,
                bitIndex, FastLog.log2(intMSB));
    }

    public Collection<Object[]> parametersForTestLog2Int() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        Random rand = new Random();

        int intMSB;
        for (int i = 0; i < Integer.SIZE; ++i) {
            intMSB = (int) Math.pow(2, i);
            intMSB += rand.nextInt(intMSB);
            list.add(new Object[]{i, intMSB});
        }

        return list;
    }

}
