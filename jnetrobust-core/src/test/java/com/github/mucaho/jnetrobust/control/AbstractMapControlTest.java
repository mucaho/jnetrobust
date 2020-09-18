/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.control;

import mockit.Deencapsulation;
import com.github.mucaho.jnetrobust.ProtocolConfig;

import java.nio.ByteBuffer;

public abstract class AbstractMapControlTest {
    protected final static ProtocolConfig config = new ProtocolConfig();
    protected static short dataId = Short.MIN_VALUE;
    protected static AbstractSegmentMap dataMap;

    protected static ByteBuffer serializeShort(short dataId) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.SIZE / Byte.SIZE);
        buffer.putShort(dataId);
        buffer.flip();
        return buffer;
    }

    protected static short deserializeShort(ByteBuffer data) {
        return data.getShort();
    }

    protected static ByteBuffer serializeInt(int dataId) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        buffer.putInt(dataId);
        buffer.flip();
        return buffer;
    }

    protected static int deserializeInt(ByteBuffer data) {
        return data.getInt();
    }

    protected static ByteBuffer serializeShorts(Short[] numbers) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE + numbers.length * Short.SIZE / Byte.SIZE);
        buffer.putInt(numbers.length);
        for (short number : numbers) buffer.putShort(number);
        buffer.flip();
        return buffer;
    }

    protected static Short[] deserializeShorts(ByteBuffer data) {
        int size = data.getInt();
        Short[] numbers = new Short[size];
        for (int i = 0; i < size; ++i) {
            numbers[i] = data.getShort();
        }
        return numbers;
    }

    protected static void initDataMap(AbstractMapControl handler) {
        dataMap = Deencapsulation.getField(handler, "dataMap");
    }

    protected final Segment addData(int data, Short... references) {
        Segment segment = new Segment(++dataId, serializeShort((short)data));
        for (short reference : references) {
            dataMap.put(reference, segment);
        }
        return segment;
    }

}
