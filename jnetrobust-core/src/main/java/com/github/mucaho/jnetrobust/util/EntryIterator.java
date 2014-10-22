/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.util;


/**
 * A reusable Iterator that has no internal state.
 *
 * @param <K> The key type.
 * @param <V> The value (iterator) type.
 * @author mucaho
 */
public interface EntryIterator<K, V> {

    /**
     * Returns the next key higher than the current key.
     *
     * @param currentKey the current key to be used in the lookup of the higher key
     *                   or null to retrieve the first key
     * @return <K> the next key higher than the current key or the first key
     * or null if there is no higher key
     */
    public K getHigherKey(K currentKey);

    /**
     * Returns the next key lower than the current key.
     *
     * @param currentKey the current key to be used in the lookup of the lower key
     *                   or null to retrieve the last key
     * @return <K> the previous key lower than the current key or the last key
     * or null if there is no lower key
     */
    public K getLowerKey(K currentKey);

    /**
     * Returns the value at the current key.
     *
     * @param currentKey the current key that is mapped to the returned value
     * @return <V> the value bound to the current key
     */
    public V getValue(K currentKey);

    /**
     * Removes the mapping the current key.
     *
     * @param currentKey the current key that is to be removed
     * @return <V> the value bound to the current key
     */
    public V removeValue(K currentKey);
}
