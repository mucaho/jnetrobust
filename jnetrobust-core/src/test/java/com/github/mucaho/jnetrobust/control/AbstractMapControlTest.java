/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import mockit.Deencapsulation;
import com.github.mucaho.jnetrobust.ProtocolConfig;

public abstract class AbstractMapControlTest {
    protected final static ProtocolConfig<Object> config = new ProtocolConfig<Object>(null);
    protected static short dataId = Short.MIN_VALUE;
    protected static AbstractMetadataMap<Object> dataMap;

    protected static void initDataMap(AbstractMapControl<Object> handler) {
        dataMap = Deencapsulation.getField(handler, "dataMap");
    }

    protected final Metadata<Object> addData(Object data, Short... references) {
        Metadata<Object> metadata = new Metadata<Object>(++dataId, data);
        for (short reference : references) {
            dataMap.put(reference, metadata);
        }
        return metadata;
    }

}
