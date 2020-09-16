/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import com.github.mucaho.jnetrobust.util.IdComparator;

public class NewestDataControl<T> {
    public interface NewestDataListener<T> {
        void handleNewestData(short dataId, T data);
    }
    private final NewestDataListener<T> listener;

    public NewestDataControl(NewestDataListener<T> listener) {
        this.listener = listener;
    }

    private boolean newestDataChanged = false;
    private Segment<T> newestSegment;

    public void refreshNewestData(Segment<T> segment) {
        if (newestSegment == null || IdComparator.instance.compare(segment.getDataId(), newestSegment.getDataId()) > 0) {
            newestSegment = segment;
            newestDataChanged = true;
        }
    }

    public void emitNewestData() {
        if (newestDataChanged && newestSegment != null)
            listener.handleNewestData(newestSegment.getDataId(), newestSegment.getData());
        newestDataChanged = false;
    }
}
