/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;

import java.nio.ByteBuffer;

public final class ByteBufferUtils {
    public final static int getDataSize(ByteBuffer data) {
        return data != null && (data.limit() < data.capacity()) ? data.limit() : 0;
    }
}
