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

public class ReceivedBitsControl {
    /*
     * [remoteTransmissionId-32]-[remoteTransmissionId-31]-...-[remoteTransmissionId-1]
     */
    private final ShiftableBitSet receivedRemoteBits = new ShiftableBitSet();

    public long getReceivedRemoteBits() {
        return this.receivedRemoteBits.get();
    }

    public void addToReceived(NavigableSet<Short> remoteTransmissionIds, short remoteTransmissionId) {
        int diff;

        Short id = remoteTransmissionIds.isEmpty() ? null : remoteTransmissionIds.first();
        while (id != null) {
            diff = IdComparator.instance.compare(remoteTransmissionId, id);
            addToReceived(diff);

            id = remoteTransmissionIds.higher(id);
        }
    }

    protected void addToReceived(int diff) {
        // add to received bitset
        if ((diff > 0) && (diff - OFFSET < SIZE)) { // save late pkg.seq into bitset
            receivedRemoteBits.set(diff - OFFSET, true);
        } else if (diff < 0) { // save new remoteTransmissionId: save old remoteTransmissionId into bitSet, then shift for remainder
            receivedRemoteBits.shiftLeft(OFFSET);
            receivedRemoteBits.setLowestBit(true);
            receivedRemoteBits.shiftLeft(-diff - OFFSET);
        }
    }
}
