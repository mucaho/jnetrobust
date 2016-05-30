/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import java.util.Comparator;

public class TimeComparator implements Comparator<Timestamp> {
    public final static TimeComparator instance = new TimeComparator();

    @Override
    public int compare(Timestamp o1, Timestamp o2) {
        return o1.getTime() == o2.getTime() ? 0 : (o1.getTime() < o2.getTime() ? -1 : 1);
    }
}
