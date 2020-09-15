/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import java.util.Comparator;

public final class IdComparator implements Comparator<Short> {
    public static final IdComparator instance = new IdComparator();

    public static final int MAX_SEQUENCE = Short.MAX_VALUE - Short.MIN_VALUE + 1;

    @Override
    public final int compare(Short seq1, Short seq2) {
        return compare(seq1.shortValue(), seq2.shortValue());
    }

    public final int compare(Short seq1, short seq2) {
        return compare(seq1.shortValue(), seq2);
    }

    public final int compare(short seq1, Short seq2) {
        return compare(seq1, seq2.shortValue());
    }

    public final int compare(short seq1, short seq2) {
        return difference(seq1 - Short.MIN_VALUE, seq2 - Short.MIN_VALUE, MAX_SEQUENCE);
    }

    public final boolean equals(Short seq1, Short seq2) {
        return equals(seq1.shortValue(), seq2.shortValue());
    }

    public final boolean equals(Short seq1, short seq2) {
        return equals(seq1.shortValue(), seq2);
    }

    public final boolean equals(short seq1, Short seq2) {
        return equals(seq1, seq2.shortValue());
    }

    public final boolean equals(short seq1, short seq2) {
        return seq1 == seq2;
    }

    /*
     * Example: maxSequence = 100; maxSequence/2 = 50;
     * seq1 = 10;
     * seq2 = 30;
     * diff = -20;
     * abs(-20 ) <= 50  ==>  return -20;
     *
     * seq1 = 30;
     * seq2 = 10;
     * diff = 20;
     * abs(20) <= 50  ==>  return 20;
     *
     *
     * seq1 = 70;
     * seq2 = 10;
     * diff = 60;
     * abs(60) !<= 50  ==>  return (-1 * 100) + 60 = -40
     *
     * seq1 = 10;
     * seq2 = 70;
     * diff = -60;
     * abs(-60) !<= 50  ==> return (--1 * 100) - 60 = 40
     * 
     */
    private static final int difference(int seq1, int seq2, int maxSequence) {
        int diff = seq1 - seq2;
        if (Math.abs(diff) <= maxSequence / 2)
            return diff;
        else
            return (-Integer.signum(diff) * maxSequence) + diff;
    }
}
