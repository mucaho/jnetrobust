package net.ddns.mucaho.jnetrobust.util;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class SequenceComparatorTest {

    public Collection<Object[]> parametersForTestCompare() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        int oldestEntryOffset = (BitConstants.OFFSET + BitConstants.SIZE) * 2;

        list.addAll(Arrays.asList(new Object[][]{
                {Short.MIN_VALUE, Short.MAX_VALUE, 1},
                {Short.MAX_VALUE, Short.MIN_VALUE, -1},
                {Short.MIN_VALUE, Short.MIN_VALUE, 0},
                {Short.MAX_VALUE, Short.MAX_VALUE, 0},
                {(short) -8, (short) -10, 2},
                {(short) -10, (short) -8, -2},
                {(short) (-20 + oldestEntryOffset), (short) 0, -20 + oldestEntryOffset},
                {(short) (-20 - oldestEntryOffset + oldestEntryOffset), (short) 0, -20}
        }));


        Random rand = new Random();
        short randomSeq1;
        short randomSeq2;
        int diff;
        final int MAX_SEQUENCE = SequenceComparator.MAX_SEQUENCE;

        for (int i = 0; i < 100; ++i) {
            randomSeq1 = (short) rand.nextInt(Short.MAX_VALUE);
            randomSeq2 = (short) rand.nextInt(Short.MAX_VALUE);
            diff = randomSeq1 - randomSeq2;
            list.add(new Object[]{randomSeq1, randomSeq2, diff});
        }

        for (int i = 0; i < 100; ++i) {
            randomSeq1 = (short) (rand.nextInt(MAX_SEQUENCE / 4) + MAX_SEQUENCE / 4 - 1);
            randomSeq2 = (short) -(rand.nextInt(MAX_SEQUENCE / 4) + MAX_SEQUENCE / 4 - 1);
            diff = randomSeq1 - randomSeq2;
            diff = MAX_SEQUENCE - diff;
            list.add(new Object[]{randomSeq1, randomSeq2, -diff});
            list.add(new Object[]{randomSeq2, randomSeq1, diff});
        }

        return list;
    }

    @Test
    @Parameters
    public final void testCompare(short seq1, short seq2, int expectedDiff) {
        assertEquals("compare(" + seq1 + ", " + seq2 + ") != " + expectedDiff,
                expectedDiff, SequenceComparator.instance.compare(seq1, seq2));
    }
}
