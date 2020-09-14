/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import java.util.*;

public final class TimeoutHandler<T extends Timestamp> {
    // TODO: add everywhere meaningful initial capacities, e.g. in this case MAX_PACKET_QUEUE_LIMIT
    private final List<T> timeouts = new ArrayList<T>();
    private final List<T> timeoutsOut = Collections.unmodifiableList(timeouts);

    public List<T> filterTimedOut(NavigableSet<T> datas, long maxWaitTime) {
        timeouts.clear();

        T data = datas.isEmpty() ? null : datas.first();
        while (data != null) {
            if (System.currentTimeMillis() - data.getTime() > maxWaitTime) {
                timeouts.add(data);
            }

            data = datas.higher(data);
        }

        return timeoutsOut;
    }

    private final List<T> sorted = new ArrayList<T>();
    private final List<T> sortedOut = Collections.unmodifiableList(sorted);

    public List<T> computeSortedByAge(Collection<T> datas) {
        sorted.clear();
        sorted.addAll(datas);

        Collections.sort(sorted, TimeComparator.instance);

        return sortedOut;
    }
}
