/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import com.github.mucaho.jnetrobust.control.Segment;

import java.util.Comparator;

public final class SentSegmentComparator implements Comparator<Segment<?>> {
    public static final SentSegmentComparator instance = new SentSegmentComparator();

    @Override
    public int compare(Segment<?> o1, Segment<?> o2) {
        Short lastTransmissionId1 = o1.getLastTransmissionId();
        Short lastTransmissionId2 = o2.getLastTransmissionId();

        int lastTransmissionIdComparison;
        if (lastTransmissionId1 == null && lastTransmissionId2 == null) {
            lastTransmissionIdComparison = 0;
        } else if (lastTransmissionId1 == null) {
            lastTransmissionIdComparison = -1;
        } else if (lastTransmissionId2 == null) {
            lastTransmissionIdComparison = 1;
        } else {
            lastTransmissionIdComparison = IdComparator.instance.compare(lastTransmissionId1, lastTransmissionId2);
        }

        // must be equal-consistent, 0 return value only allowed iff dataIds same!
        int dataIdComparison = IdComparator.instance.compare(o1.getDataId(), o2.getDataId());
        return dataIdComparison == 0 || lastTransmissionIdComparison == 0
                ? dataIdComparison
                : lastTransmissionIdComparison;
    }
}
