/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.ddns.mucaho.jnetrobust.util;

import java.io.Externalizable;
import java.io.Serializable;

public interface Freezable<T> extends Serializable, Externalizable, Cloneable {
    public T clone();
}
