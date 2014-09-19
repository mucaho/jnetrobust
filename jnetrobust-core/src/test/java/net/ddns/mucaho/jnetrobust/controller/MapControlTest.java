package net.ddns.mucaho.jnetrobust.controller;

import mockit.Deencapsulation;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValueMap;
import net.ddns.mucaho.jnetrobust.util.Config;

public abstract class MapControlTest {
    protected final static Config config = new Config(null);
    protected static short dataId = Short.MIN_VALUE;
    protected static MultiKeyValueMap dataMap;

    protected static void initDataMap(MapControl handler) {
        dataMap = Deencapsulation.getField(handler, "dataMap");
    }

    protected final MultiKeyValue addData(Object data, Short... references) {
        MultiKeyValue multiRef = new MultiKeyValue(++dataId, data);
        for (short reference : references) {
            dataMap.put(reference, multiRef);
        }
        return multiRef;
    }

}
