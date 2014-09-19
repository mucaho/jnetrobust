package net.ddns.mucaho.jnetrobust.controller;

import java.util.Collection;

import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.util.TimeoutHandler;

public class ResponseControl {
	private final TimeoutHandler<MultiKeyValue> pendingDataTimeoutHandler = 
		new TimeoutHandler<MultiKeyValue>();
	
	private final Collection<MultiKeyValue> pendingDatas;
	
	public ResponseControl(Collection<MultiKeyValue> pendingDatas) {
		this.pendingDatas = pendingDatas;
	}
	
	protected Collection<MultiKeyValue> updatePendingTime(long maxWaitTime) {
        return pendingDataTimeoutHandler.filterTimedOut(pendingDatas, maxWaitTime);
	}
}
