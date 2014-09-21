package net.ddns.mucaho.jnetrobust.example;


import com.esotericsoftware.kryo.Kryo;
import net.ddns.mucaho.jnetrobust.control.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.controller.Packet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;

public class ObjectSynchronizationController implements Runnable {

    private ObjectSynchronizationGUI gui;
    private DefaultHost<Vector2D> host;
    private final ObjectSynchronization.MODE updateMode;
    private final Vector2D data;

    public ObjectSynchronizationController(ObjectSynchronization.HOST hostMode, final ObjectSynchronization.MODE updateMode,
                                           InetSocketAddress hostAddress, InetSocketAddress receiverAddress) throws IOException {

        this.updateMode = updateMode;
        data = new Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE, hostMode);

        gui = new ObjectSynchronizationGUI(hostMode);
        gui.setVisible(true);

        Kryo serializer = new Kryo();
        serializer.register(Vector2D.class);
        serializer.register(MultiKeyValue.class);
        serializer.register(Packet.class);


        host = new DefaultHost<Vector2D>(hostMode.toString(), serializer,
                hostAddress, receiverAddress, new DefaultHost.OrderedDataListener<Vector2D>() {
            @Override
            public void handleOrderedData(final Vector2D orderedData) {
                if (updateMode == ObjectSynchronization.MODE.UPDATE_ON_ORDERED_DATA)
                    gui.updateGUI(orderedData);
            }
        });
    }



    @Override
    public void run() {
        try {
            // receive
            Queue<Vector2D> receivedQueue = host.receive();
            for (final Vector2D receivedData : receivedQueue)
                if (updateMode == ObjectSynchronization.MODE.UPDATE_ON_RECEIVED_DATA)
                    gui.updateGUI(receivedData);

            // send
            gui.sendGUI(data);
            host.send(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class Vector2D {
        private ObjectSynchronization.HOST host;
        private int x;
        private int y;

        public Vector2D() {
        }

        public Vector2D(int x, int y, ObjectSynchronization.HOST host) {
            this.x = x;
            this.y = y;
            this.host = host;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public ObjectSynchronization.HOST getHost() {
            return host;
        }

        public void setHost(ObjectSynchronization.HOST host) {
            this.host = host;
        }

        @Override
        public String toString() {
            return "Vector2D{" +
                    "host=" + host +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

}