/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

public final class BitConstants {
    public static final int OFFSET = 1;
    public static final int SIZE = Long.SIZE;
    public static final long MSB = 0x8000000000000000L;
    public static final long LSB = 0x1L;
    private static final long INT_MASK = 0xFFFFFFFFL;

    private BitConstants() {
    }

    public static final long convertBits(int bits) {
        return ((long) bits) & INT_MASK;
    }

    public static final int convertBits(long bits) {
        return (int) bits;
    }
}
