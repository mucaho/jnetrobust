package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.TimeoutHandler;

import java.util.Collection;

public class ResponseControl {
    private final TimeoutHandler<Metadata> pendingDataTimeoutHandler =
            new TimeoutHandler<Metadata>();

    private final Collection<Metadata> pendingDatas;

    public ResponseControl(Collection<Metadata> pendingMetadatas) {
        this.pendingDatas = pendingMetadatas;
    }

    public void resetPendingTime(Metadata pendingMetadata) {
        pendingMetadata.updateTime();
    }

    public Collection<Metadata> updatePendingTime(long maxWaitTime) {
        return pendingDataTimeoutHandler.filterTimedOut(pendingDatas, maxWaitTime);
    }
}
