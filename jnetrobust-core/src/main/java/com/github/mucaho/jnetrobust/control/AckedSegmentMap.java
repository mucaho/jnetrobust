/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

public class AckedSegmentMap extends SentSegmentMap {

    @Override
    void putAll(Segment segment) {
        throw new UnsupportedOperationException("Can not add segment without associated transmissionId!");
    }

    @Override
    Segment put(Segment segment) {
        throw new UnsupportedOperationException("Can not add segment without associated transmissionId!");
    }
}
