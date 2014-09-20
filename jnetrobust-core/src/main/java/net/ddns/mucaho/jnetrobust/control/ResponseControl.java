package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.util.TimeoutHandler;

import java.util.Collection;

public class ResponseControl {
    private final TimeoutHandler<MultiKeyValue> pendingDataTimeoutHandler =
            new TimeoutHandler<MultiKeyValue>();

    private final Collection<MultiKeyValue> pendingDatas;

    public ResponseControl(Collection<MultiKeyValue> pendingDatas) {
        this.pendingDatas = pendingDatas;
    }

    public Collection<MultiKeyValue> updatePendingTime(long maxWaitTime) {
        return pendingDataTimeoutHandler.filterTimedOut(pendingDatas, maxWaitTime);
    }
}
