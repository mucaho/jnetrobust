package net.ddns.mucaho.jnetrobust.control;

import net.ddns.mucaho.jnetrobust.util.TimeoutHandler;

import java.util.Collection;

public class ResponseControl<T> {
    private final TimeoutHandler<Metadata<T>> pendingDataTimeoutHandler =
            new TimeoutHandler<Metadata<T>>();

    private final Collection<Metadata<T>> pendingDatas;

    public ResponseControl(Collection<Metadata<T>> pendingMetadatas) {
        this.pendingDatas = pendingMetadatas;
    }

    public void resetPendingTime(Metadata<T> pendingMetadata) {
        pendingMetadata.updateTime();
    }

    public Collection<Metadata<T>> updatePendingTime(long maxWaitTime) {
        return pendingDataTimeoutHandler.filterTimedOut(pendingDatas, maxWaitTime);
    }
}
