/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BitConstantsTest {
    @Test
    public void testConvertBits_IntToLong() {
        for (
                int i = Integer.MIN_VALUE, step = Integer.MAX_VALUE / 100, max = Integer.MAX_VALUE;
                i < max;
                i = (i > 0) && (i + step < 0) ? Integer.MAX_VALUE : i + step
        ) {
            String intBits = Integer.toBinaryString(i);
            String longBits = Long.toBinaryString(BitConstants.convertBits(i));
            assertEquals(intBits, longBits);
        }
    }

    @Test
    public void testConvertBits_LongToInt() {
        for (
                long i = Long.MIN_VALUE, step = Long.MAX_VALUE / 100, max = Long.MAX_VALUE;
                i < max;
                i = (i > 0) && (i + step < 0) ? Long.MAX_VALUE : i + step
        ) {
            String longBits = Long.toBinaryString(i);
            String intBits = Integer.toBinaryString(BitConstants.convertBits(i));

            String trimmedLongBits = String.format("%32s",
                    longBits.length() <= 32 ? longBits : longBits.substring(longBits.length() - 32)
            ).replace(' ', '0');
            String paddedIntBits = String.format("%32s", intBits).replace(' ', '0');

            assertEquals(trimmedLongBits, paddedIntBits);
        }
    }
}