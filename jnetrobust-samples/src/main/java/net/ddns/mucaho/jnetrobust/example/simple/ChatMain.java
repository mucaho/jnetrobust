package net.ddns.mucaho.jnetrobust.example.simple;


import net.ddns.mucaho.jnetrobust.example.DefaultHost;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Queue;

public class ChatMain {
    public static void main (String[] args) throws Exception {
        // host addresses
        InetSocketAddress ADDRESS_A = new InetSocketAddress(InetAddress.getLocalHost(), 12345);
        InetSocketAddress ADDRESS_B = new InetSocketAddress(InetAddress.getLocalHost(), 12346);

        // setup DefaultHost A
        DefaultHost<String> hostA = new DefaultHost<String>("A", ADDRESS_A, ADDRESS_B,
                String.class, new DefaultHost.DataListener<String>() {
            @Override
            public void handleOrderedData(String orderedData) {}
            @Override
            public void handleNewestData(String newestData) {}
        });

        // setup DefaultHost B
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
