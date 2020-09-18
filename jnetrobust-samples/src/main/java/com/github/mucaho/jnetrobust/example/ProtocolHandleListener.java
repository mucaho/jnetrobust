/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.example;

import com.github.mucaho.jnetrobust.Logger;

import java.io.Serializable;

public interface ProtocolHandleListener<T extends Serializable> {
    class ProtocolException extends RuntimeException {
        private final Logger.LoggingEvent loggingEvent;
        private final Serializable data;

        public ProtocolException(Logger.LoggingEvent loggingEvent, Serializable data) {
            super();
            this.loggingEvent = loggingEvent;
            this.data = data;
        }

        public Logger.LoggingEvent getLoggingEvent() {
            return loggingEvent;
        }

        @SuppressWarnings("unchecked")
        public <T extends Serializable> T getData() {
            return (T) data;
        }
    }

    void handleOrderedData(T orderedData);

    void handleNewestData(T newestData);

    void handleExceptionalData(Exception e);
}
