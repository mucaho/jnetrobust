/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Deencapsulation;
import com.github.mucaho.jnetrobust.util.ShiftableBitSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static com.github.mucaho.jnetrobust.util.BitConstants.*;
import static com.github.mucaho.jnetrobust.util.TestUtils.binLong;
import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class ReceivedBitsControlTest {

    @Test
    @Parameters
    public final void testAddToReceivedInt(long preBits, int inDiff, long postBits) {
        ShiftableBitSet actualBitSet = new ShiftableBitSet(preBits);
        ShiftableBitSet expectedBitSet = new ShiftableBitSet(postBits);
        ReceivedBitsControl handler = new ReceivedBitsControl();
        Deencapsulation.setField(handler, "receivedRemoteBits", actualBitSet);

        String debug = "";
        debug += actualBitSet;
        handler.addToReceived(inDiff);
        debug += ".addToReceived(" + inDiff + ")";
        debug += " = " + actualBitSet;
        debug += " != " + expectedBitSet;

        assertEquals(debug, expectedBitSet.get(), actualBitSet.get());
    }

    public Collection<Object[]> parametersForTestAddToReceivedInt() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        Random rng = new Random();
        long preBits;
        long postBits;
        int index;


        list.add(new Object[]{0L, 0, 0L});
        list.add(new Object[]{0L, OFFSET + SIZE, 0L});
        list.add(new Object[]{binLong("0100"), OFFSET, binLong("0101")});
        list.add(new Object[]{binLong("0100"), -OFFSET, binLong("1001")});

        for (int i = 0; i < 100; ++i) {
            preBits = rng.nextLong();
            index = rng.nextInt(OFFSET + SIZE - 1) + 1;

            postBits = preBits | LSB << index - OFFSET;
            list.add(new Object[]{preBits, index, postBits});

            postBits = preBits << OFFSET | LSB;
            postBits <<= index - OFFSET;
            list.add(new Object[]{preBits, -index, postBits});
        }
        return list;
    }

}
