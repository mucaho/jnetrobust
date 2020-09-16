/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.example;

import java.io.Serializable;

public interface ProtocolHandleListener<T extends Serializable> {
    void handleOrderedData(T orderedData);

    void handleNewestData(T newestData);

    void handleExceptionalData(T exceptionalData);
}
