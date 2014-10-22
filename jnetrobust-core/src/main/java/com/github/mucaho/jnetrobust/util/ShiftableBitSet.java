/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import static com.github.mucaho.jnetrobust.util.BitConstants.LSB;
import static com.github.mucaho.jnetrobust.util.BitConstants.SIZE;

public class ShiftableBitSet {
    private long bits;


    public ShiftableBitSet(long bits) {
        super();
        this.bits = bits;
    }

    public ShiftableBitSet() {
        this.bits = 0;
    }


    public void and(long i) {
        this.bits &= i;
    }

    public void or(long i) {
        this.bits |= i;
    }

    public void xor(long i) {
        this.bits ^= i;
    }


    public void minus(long i) {
        this.bits -= i;
    }

    public void plus(long i) {
        this.bits += i;
    }


    public void set(long i) {
        this.bits = i;
    }

    public long get() {
        return this.bits;
    }

    public int getInt() {
        return (int) bits;
    }


    public void shift(int i) {
        if (i >= 0) {
            this.shiftLeft(i);
        } else {
            this.shiftRight(-i);
        }
    }

    public void shiftRight(int i) {
        bits >>>= i;
    }

    public void shiftLeft(int i) {
        bits <<= i;
    }


    private void setBit(int index, boolean set) {
        if (set) {
            bits |= LSB << index;
        } else {
            bits &= ~(LSB << index);
        }
    }

    public void setHighestBit(boolean set) {
        this.setBit(SIZE - 1, set);
    }

    public void setLowestBit(boolean set) {
        this.setBit(0, set);
    }

    public void set(int index, boolean set) {
        if (index < 0) { // add negative index bit (shift bitset left and set bit)
            this.shiftLeft(-index);
            this.setLowestBit(set);
        } else if (index < SIZE) { // set to positive 0 <= index < Size
            this.setBit(index, set);
        } else { // non-existing index, right shift the bitset then set the bit
            this.shiftRight(index - SIZE + 1);
            this.setHighestBit(set);
        }
    }

    @Override
    public String toString() {
        return String.format("%64s", Long.toBinaryString(bits));
    }
}
