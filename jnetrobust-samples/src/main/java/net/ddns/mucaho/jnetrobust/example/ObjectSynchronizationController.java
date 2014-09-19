package net.ddns.mucaho.jnetrobust.example;


import com.esotericsoftware.kryo.Kryo;
import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;
import net.ddns.mucaho.jnetrobust.data.Packet;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;

public class ObjectSynchronizationController implements Runnable {
    public static enum HOST {CLIENT, SERVER}

    public static enum MODE {UPDATE_ON_RECEIVED_DATA, UPDATE_ON_ORDERED_DATA}

    private final ObjectSynchronizationGUI gui;
    private final DefaultHost<Vector2D> host;
    private final MODE updateMode;

    public ObjectSynchronizationController(HOST hostMode, final MODE updateMode, InetSocketAddress hostAddress,
                                           InetSocketAddress receiverAddress) throws IOException {
        this.updateMode = updateMode;

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
                if (updateMode == MODE.UPDATE_ON_ORDERED_DATA)
                    updateGUI(orderedData);
            }
        });

    }

    private Vector2D oldData = new Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE);

    @Override
    public void run() {
        try {
            // receive
            Queue<Vector2D> receivedQueue = host.receive();
            for (final Vector2D receivedData : receivedQueue)
                if (updateMode == MODE.UPDATE_ON_RECEIVED_DATA)
                    updateGUI(receivedData);

            // send
            sendGUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendGUI() throws IOException {
        boolean isNewData = false;

        JPanel hostObject = gui.getHostObject();
        if (hostObject.getX() != oldData.getX() || hostObject.getY() != oldData.getY())
            isNewData = true;

        oldData.setX(hostObject.getX());
        oldData.setY(hostObject.getY());

        //FIXME if (isNewData)
        host.send(oldData);
    }

    private void updateGUI(final Vector2D data) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JPanel remoteObject = gui.getRemoteObject();
                if (remoteObject.getX() != data.getX() || remoteObject.getY() != data.getY())
                    remoteObject.setLocation(data.getX(), data.getY());
            }
        });
    }


    public static class Vector2D {
        private int x;
        private int y;

        public Vector2D() {
        }

        public Vector2D(int x, int y) {
            this.x = x;
            this.y = y;
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

        @Override
        public String toString() {
            return "{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

}