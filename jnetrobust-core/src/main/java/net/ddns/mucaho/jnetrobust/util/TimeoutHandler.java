/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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

        for (T data : pendingDatas) {
            if (System.currentTimeMillis() - data.getTime() > maxWaitTime) {
                timeouts.add(data);
            }
        }

        return timeoutsOut;
    }
}
