/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;

import java.nio.ByteBuffer;

public class NewestReceivedControl {
    public interface NewestReceivedListener {
        void handleNewestData(short dataId, ByteBuffer data);
    }
    private final NewestReceivedListener listener;

    public NewestReceivedControl(NewestReceivedListener listener) {
        this.listener = listener;
    }

    private boolean newestReceivedChanged = false;
    private Segment newestReceivedSegment = null;

    public void refreshNewestReceived(Segment segment) {
        if (newestReceivedSegment == null || IdComparator.instance.compare(segment.getDataId(), newestReceivedSegment.getDataId()) > 0) {
            newestReceivedSegment = segment;
            newestReceivedChanged = true;
        }
    }

    public void emitNewestReceived() {
        if (newestReceivedChanged && newestReceivedSegment != null) {
            listener.handleNewestData(newestReceivedSegment.getDataId(), newestReceivedSegment.getData());
            if (newestReceivedSegment.getData() != null) newestReceivedSegment.getData().rewind();
        }
        newestReceivedChanged = false;
    }
}
