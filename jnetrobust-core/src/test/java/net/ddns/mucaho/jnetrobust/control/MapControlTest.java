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
