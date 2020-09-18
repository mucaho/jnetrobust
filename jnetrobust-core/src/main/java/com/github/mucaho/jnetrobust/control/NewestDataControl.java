/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;

import java.nio.ByteBuffer;

public class NewestDataControl {
    public interface NewestDataListener {
        void handleNewestData(short dataId, ByteBuffer data);
    }
    private final NewestDataListener listener;

    public NewestDataControl(NewestDataListener listener) {
        this.listener = listener;
    }

    private boolean newestDataChanged = false;
    private Segment newestSegment;

    public void refreshNewestData(Segment segment) {
        if (newestSegment == null || IdComparator.instance.compare(segment.getDataId(), newestSegment.getDataId()) > 0) {
            newestSegment = segment;
            newestDataChanged = true;
        }
    }

    public void emitNewestData() {
        if (newestDataChanged && newestSegment != null) {
            listener.handleNewestData(newestSegment.getDataId(), newestSegment.getData());
            if (newestSegment.getData() != null) newestSegment.getData().rewind();
        }
        newestDataChanged = false;
    }
}
