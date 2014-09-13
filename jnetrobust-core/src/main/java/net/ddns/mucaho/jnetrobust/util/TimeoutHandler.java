package net.ddns.mucaho.jnetrobust.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class TimeoutHandler<T extends Timestamp> {
	private final Set<T> timeouts = new LinkedHashSet<T>();
	private final Set<T> timeoutsOut = Collections.unmodifiableSet(timeouts);
	
	public Collection<T> filterTimedOut(Collection<T> pendingDatas, long maxWaitTime) {
		timeouts.clear();
		
		for (T data: pendingDatas) {
			if (System.currentTimeMillis() - data.getTime() > maxWaitTime) {
				timeouts.add(data);
			}
		}
		
		return timeoutsOut;
	}
}
