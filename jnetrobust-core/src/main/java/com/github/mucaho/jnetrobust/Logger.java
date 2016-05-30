/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust;

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
     * Retrieve a simple logger which logs events to the {@link java.lang.System#out System.out} stream.
     * @param name  the name of the protocol instance
     * @return  a new console <code>Logger</code>
     */
    public static Logger getConsoleLogger(final String name) {
        return new Logger() {
            @Override
            public void log(String description, Object... params) {
                System.out.print("[" + name + "]: " + description + "\t");
                for (int i = 0, l = params.length; i < l; ++i)
                    System.out.print(params[i] + "\t");
                System.out.println();
            }
        };
    }
}
