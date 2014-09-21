package net.ddns.mucaho.jnetrobust.control;

import mockit.Deencapsulation;
import net.ddns.mucaho.jnetrobust.ProtocolConfig;

public abstract class MapControlTest {
    protected final static ProtocolConfig config = new ProtocolConfig(null);
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
