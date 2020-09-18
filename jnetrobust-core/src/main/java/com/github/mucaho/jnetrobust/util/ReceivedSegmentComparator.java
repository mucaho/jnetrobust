/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import com.github.mucaho.jnetrobust.control.Segment;

import java.util.Comparator;

public final class ReceivedSegmentComparator implements Comparator<Segment> {
    public static final ReceivedSegmentComparator instance = new ReceivedSegmentComparator();

    @Override
    public int compare(Segment o1, Segment o2) {
        return IdComparator.instance.compare(o1.getDataId(), o2.getDataId());
    }
}
