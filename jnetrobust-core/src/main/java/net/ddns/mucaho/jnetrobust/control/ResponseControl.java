package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.TimeoutHandler;

import java.util.Collection;

public class ResponseControl {
    private final TimeoutHandler<MetadataUnit> pendingDataTimeoutHandler =
            new TimeoutHandler<MetadataUnit>();

    private final Collection<MetadataUnit> pendingDatas;

    public ResponseControl(Collection<MetadataUnit> pendingMetadatas) {
        this.pendingDatas = pendingMetadatas;
    }

    public void resetPendingTime(MetadataUnit pendingMetadata) {
        pendingMetadata.updateTime();
    }

    public Collection<MetadataUnit> updatePendingTime(long maxWaitTime) {
        return pendingDataTimeoutHandler.filterTimedOut(pendingDatas, maxWaitTime);
    }
}
