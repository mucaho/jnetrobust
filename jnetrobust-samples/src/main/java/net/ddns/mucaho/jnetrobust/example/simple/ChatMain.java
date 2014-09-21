package net.ddns.mucaho.jnetrobust.example.simple;


import net.ddns.mucaho.jnetrobust.example.DefaultHost;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Queue;

public class ChatMain {
    private static InetSocketAddress ADDRESS_A;
    private static InetSocketAddress ADDRESS_B;

    static {
        try {
            ADDRESS_A = new InetSocketAddress(InetAddress.getLocalHost(), 12345);
            ADDRESS_B = new InetSocketAddress(InetAddress.getLocalHost(), 12346);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        // setup host A
        DefaultHost<String> hostA = new DefaultHost<String>("A", ADDRESS_A, ADDRESS_B,
                String.class, new DefaultHost.DataListener<String>() {
            @Override
            public void handleOrderedData(String orderedData) {}
            @Override
            public void handleNewestData(String newestData) {}
        });

        // setup host B
        DefaultHost<String> hostB = new DefaultHost<String>("B", ADDRESS_B, ADDRESS_A,
                String.class, new DefaultHost.DataListener<String>() {
            @Override
            public void handleOrderedData(String orderedData) {}
            @Override
            public void handleNewestData(String newestData) {}
        });

        Queue<String> receivedMessages;

        // send from A
        hostA.send("Hi!");
        hostA.send("How you doing?");

        Thread.sleep(100);

        // receive at B
        receivedMessages = hostB.receive();
        for (String receivedMessage: receivedMessages)
            System.out.println("[B]: "+receivedMessage);

        // send from B
        hostB.send("Howdy! Fine, thanks.");

        Thread.sleep(100);

        // receive at A
        receivedMessages = hostA.receive();
        for (String receivedMessage: receivedMessages)
            System.out.println("[A]: "+receivedMessage);
    }
}
