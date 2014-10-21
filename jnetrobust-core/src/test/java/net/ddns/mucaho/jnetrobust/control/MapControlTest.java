/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust.control;

import mockit.Deencapsulation;
import net.ddns.mucaho.jnetrobust.ProtocolConfig;

public abstract class MapControlTest {
    protected final static ProtocolConfig<Object> config = new ProtocolConfig<Object>(null);
    protected static short dataId = Short.MIN_VALUE;
    protected static MetadataMap<Object> dataMap;

    protected static void initDataMap(MapControl<Object> handler) {
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
