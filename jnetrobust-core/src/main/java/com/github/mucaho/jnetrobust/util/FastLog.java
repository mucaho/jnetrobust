/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

public final class FastLog {
    /*
     * See http://graphics.stanford.edu/~seander/bithacks.html#IntegerLog
     */
    private final static long b[] = {0x2, 0xC, 0xF0, 0xFF00, 0xFFFF0000, 0xFFFFFFFF00000000L};
    private final static int S[] = {1, 2, 4, 8, 16, 32};

    public final static int log2(long v) {
        int r = 0; // result of log2(v) will go here
        for (int i = 5; i >= 0; i--) {
            if ((v & b[i]) != 0) {
                v >>= S[i];
                r |= S[i];
            }
        }
        return r;
    }

    public final static int log2(int v) {
        int r = 0; // result of log2(v) will go here
        for (int i = 4; i >= 0; i--) {
            if ((v & b[i]) != 0) {
                v >>= S[i];
                r |= S[i];
            }
        }
        return r;
    }
}
