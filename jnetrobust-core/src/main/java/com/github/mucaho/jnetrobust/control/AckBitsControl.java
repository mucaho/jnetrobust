/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;
import com.github.mucaho.jnetrobust.util.ShiftableBitSet;

import java.util.NavigableSet;

import static com.github.mucaho.jnetrobust.util.BitConstants.OFFSET;
import static com.github.mucaho.jnetrobust.util.BitConstants.SIZE;

public class AckBitsControl {
    /*
     * [remoteTransmissionId-32]-[remoteTransmissionId-31]-...-[remoteTransmissionId-1]
     */
    private final ShiftableBitSet ackRemoteBits = new ShiftableBitSet();

    public long getAckRemoteBits() {
        return this.ackRemoteBits.get();
    }

    public void addToAck(NavigableSet<Short> remoteTransmissionIds, short remoteTransmissionId) {
        int diff;

        Short id = remoteTransmissionIds.isEmpty() ? null : remoteTransmissionIds.first();
        while (id != null) {
            diff = IdComparator.instance.compare(remoteTransmissionId, id);
            addToAck(diff);

            id = remoteTransmissionIds.higher(id);
        }
    }

    protected void addToAck(int diff) {
        // add to received bitset
        if ((diff > 0) && (diff - OFFSET < SIZE)) { // save late pkg.seq into bitset
            ackRemoteBits.set(diff - OFFSET, true);
        } else if (diff < 0) { // save new remoteTransmissionId: save old remoteTransmissionId into bitSet, then shift for remainder
            ackRemoteBits.shiftLeft(OFFSET);
            ackRemoteBits.setLowestBit(true);
            ackRemoteBits.shiftLeft(-diff - OFFSET);
        }
    }
}
