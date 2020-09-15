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
    private Metadata<T> newestMetadata;

    public void refreshNewestData(Metadata<T> metadata) {
        if (newestMetadata == null || IdComparator.instance.compare(metadata.getDataId(), newestMetadata.getDataId()) > 0) {
            newestMetadata = metadata;
            newestDataChanged = true;
        }
    }

    public void emitNewestData() {
        if (newestDataChanged && newestMetadata != null)
            listener.handleNewestData(newestMetadata.getDataId(), newestMetadata.getData());
        newestDataChanged = false;
    }
}
