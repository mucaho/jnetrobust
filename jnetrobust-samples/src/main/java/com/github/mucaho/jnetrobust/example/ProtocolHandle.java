/*
 * Copyright (c) 2014 mucaho (https://github.com/mucaho).
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.github.mucaho.jnetrobust.example;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.List;

public class ProtocolHandle<T extends Serializable> {
    private final ProtocolId protocolId;
    private final ProtocolHost host;

    ProtocolHandle(ProtocolId protocolId, ProtocolHost host) {
        this.host = host;
        this.protocolId = protocolId;
    }

    public void send() throws IOException {
        host.send(protocolId);
    }

    public void send(T data) throws IOException {
        host.send(protocolId, data);
    }

    public void send(List<T> datas) throws IOException {
        host.send(protocolId, datas);
    }

    public T receive() throws IOException, ClassNotFoundException {
        host.receive();
        return host.<T>receive(protocolId);
    }

    static class ProtocolId {
        private final byte topic;
        private final SocketAddress remoteAddress;

        ProtocolId(byte topic, SocketAddress remoteAddress) {
            this.topic = topic;
            this.remoteAddress = remoteAddress;
        }

        public byte getTopic() {
            return topic;
        }

        public SocketAddress getRemoteAddress() {
            return remoteAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ProtocolId that = (ProtocolId) o;

            if (topic != that.topic) return false;
            if (!remoteAddress.equals(that.remoteAddress)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) topic;
            result = 31 * result + remoteAddress.hashCode();
            return result;
        }
    }
}
