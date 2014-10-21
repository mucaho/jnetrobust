/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust.util;

import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PrimitiveValuesTest {

    @Test
    public final void testShort() {
        assertEquals("Short value wraparound not working.",
                Short.MIN_VALUE, (short) (Short.MAX_VALUE + 1));
        assertEquals("Short range incorrect.",
                Short.MAX_VALUE - Short.MIN_VALUE + 1, (int) Math.pow(2, 16));
    }

    @Test
    public final void testPrimitiveWrappers() {
        ArrayList<Short> list = new ArrayList<Short>();
        Short orig = 3;
        list.add(orig);
        orig = 5;
        assertEquals("Short is mutable!", (short) 3, (short) list.get(0));
    }

    @Test
    public final void testLongToIntConversion() {
        long test = Long.MIN_VALUE + 1;
        String longStr = Long.toBinaryString(test);
        String intStr = Integer.toBinaryString((int) test);
        assertThat("longStr and intStr should not match.",
                longStr, is(not(equalTo(intStr))));

        test = -1L;
        longStr = Long.toBinaryString(test);
        intStr = Integer.toBinaryString((int) test);
        assertThat("longStr and intStr should not match.",
                longStr, is(not(equalTo(intStr))));

        test = 1L;
        longStr = Long.toBinaryString(test);
        intStr = Integer.toBinaryString((int) test);
        assertThat("longStr and intStr should match.",
                longStr, is(equalTo(intStr)));

        long initialVar = Long.MIN_VALUE;
        long convertedVar = ((int) initialVar);
        assertThat("initial Var and converted Var should not match.",
                initialVar, is(not(equalTo(convertedVar))));
        initialVar &= Integer.MAX_VALUE;
        assertThat("initial Var and converted Var should match.",
                initialVar, is(equalTo(convertedVar)));


        long[] longs = {
                0xFFFFFFFFFFFFFFFFL,
                0x0000000000000001L,
                0x8000000000000001L,
                0x0000000000000000L
        };
        int[] ints = new int[4];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = (int) longs[i];
        }

        for (int i = 0; i < 4; i++) {
            String longString = Long.toBinaryString(longs[i]);
            String intString = Integer.toBinaryString(ints[i]);

            int beginIndex = longString.length() - 32;
            String smallLongString = beginIndex < 0 ? longString : longString.substring(beginIndex);
            smallLongString = smallLongString.replaceFirst("^0+(?!$)", "");

            assertThat("int representation matches long representation.",
                    intString, is(equalTo(smallLongString)));
        }
    }

}
