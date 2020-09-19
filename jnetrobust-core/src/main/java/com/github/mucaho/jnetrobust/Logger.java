/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust;

import java.nio.ByteBuffer;

/**
 * Abstract class which is able to handle protocol specific logging.
 * Implementations of this class have to implement the {@link Logger#log(String, Object...) log} method.
 */
public abstract class Logger {
    /**
     * Enum representing all possible <code>logging event descriptions</code>.
     */
    public static enum LoggingEvent {
        SEND("Data sent"),
        RECEIVE("Data received"),
        NEWEST("Newest data received"),
        SEND_RETRANSMISSION("Data retransmitted"),
        RETRANSMISSION("Data needs to be retransmitted"),
        ORDERED("Data received ordered"),
        UNORDERED("Data received not ordered"),
        ACKED("Data was received at other end"),
        NOTACKED("Data was probably not received at other end");

        private final String description;
        private LoggingEvent(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * Log changes in the protocol's state.
     * @param description   the description of the log event, matches one of the {@link Logger.LoggingEvent logging events}
     * @param params    zero or more features that are logged
     */
    public abstract void log(String description, Object... params);

    /**
     * Abstract simple logger which logs events to the {@link java.lang.System#out System.out} stream.<br />
     * Implementors should extend this class providing a suitable implementation for {@link #deserialize(ByteBuffer)}.
     */
    public abstract static class AbstractConsoleLogger extends Logger {

        private final String name;

        public AbstractConsoleLogger(String name) {
            this.name = name;
        }

        @Override
        public void log(String description, Object... params) {
            StringBuffer sb = new StringBuffer();
            sb.append("[" + name + "]: " + description + "\t");
            for (int i = 0, l = params.length; i < l; ++i) {
                if (params[i] instanceof ByteBuffer) {
                    ByteBuffer dataBuffer = (ByteBuffer) params[i];
                    dataBuffer.rewind();
                    sb.append(deserialize(dataBuffer) + "\t");
                    dataBuffer.rewind();
                } else {
                    sb.append(params[i] + "\t");
                }
            }
            sb.append("@loggerId=").append(hashCode()).append("\t@T=").append(System.currentTimeMillis());
            System.out.println(sb.toString());
        }

        protected abstract String deserialize(ByteBuffer dataBuffer);
    }
}
