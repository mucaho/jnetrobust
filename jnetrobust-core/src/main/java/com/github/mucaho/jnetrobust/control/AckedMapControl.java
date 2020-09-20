/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.SystemClock;

public class AckedMapControl extends AbstractMapControl {

    public AckedMapControl(int maxEntries, int maxEntryOffset, int maxEntryOccurrences, long maxEntryTimeout, SystemClock systemClock) {
        super(maxEntries, maxEntryOffset, maxEntryOccurrences, maxEntryTimeout, systemClock);
    }

    @Override
    protected AbstractSegmentMap createMap() {
        return new AckedSegmentMap();
    }

    public void addToAcked(Short transmissionId, Segment segment) {
        // add to acked map
        dataMap.put(transmissionId, segment);
    }

    @Override
    protected void discardEntry(Short key) {
        dataMap.removeAll(key);
    }

    @Override
    protected void discardEntry(Segment segment) {
        dataMap.removeAll(segment);
    }

    @Override
    protected void discardEntryKey(Short key) {
        dataMap.remove(key);
    }
}
