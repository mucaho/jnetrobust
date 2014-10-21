/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust.util;

public final class BitConstants {
    public final static int OFFSET = 1;
    public final static int SIZE = Long.SIZE;
    public final static long MSB = 0x8000000000000000L;
    public final static long LSB = 0x1;

    private final static long LONG_MASK = 0x7FFFFFFFFFFFFFFFL;

    public final static long convertBits(int bits) {
        return ((long) bits) & LONG_MASK;
    }

    public final static int convertBits(long bits) {
        return (int) bits;
    }
}
