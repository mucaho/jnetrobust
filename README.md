![jnetrobust](https://raw.githubusercontent.com/mucaho/jnetrobust/gh-pages/images/robust.png)
=============

Reliable &amp; lightweight virtual network protocol for the JVM.   
**Currently under construction.**

What is it?
-----------
JNetRobust is a virtual network protocol that resides between the [transport](http://en.wikipedia.org/wiki/Transport_layer) and the [application](http://en.wikipedia.org/wiki/Application_layer) layer.

It provides some benefits from both transport layer protocols that are accessible on the JVM:
* performance of [UPD](http://en.wikipedia.org/wiki/User_Datagram_Protocol)
* reliability of [TCP](http://en.wikipedia.org/wiki/Transmission_Control_Protocol)

**Benefits**
* reliability of transmitted data - like TCP   
   counters network characteristics like out-of-order delivery, package loss, package duplication
* received, unvalidated data is available immediately - like UDP   
   unlike in TCP where the protocol provides you the data only after it can guarantee in-order delivery
* the package is bigger than UDP's package, but smaller than TCP's package

**Caveats**
* no flow control - unlike TCP
* no congestion control - unlike TCP

Why should you use it?
----------------------
If you don't need reliability, use UDP.   
If you need reliability and you can wait for it to be validated, use TCP.   
If you need reliability, but you benefit from receiving unvalidated data with no latency, try JNetRobust.

How do you use it?
------------------
It is a library and imposes no restrictions on how you use it:   
* the protocol maintains an internal state, that is updated every time you use it
* the protocol just adds/removes metadata to/from your original data
* you decide what to do with the metadata-packaged data (e.g. add some of your own metadata, etc... )
* you decide how you want to serialize the metadata-packaged data (e.g. with [default serialization](http://docs.oracle.com/javase/7/docs/api/java/io/Externalizable.html), [Kryo](https://github.com/EsotericSoftware/kryo), etc... )
* you decide how you want to send the metadata-packaged data (e.g. plain [DatagramSocket](http://docs.oracle.com/javase/7/docs/api/java/net/DatagramSocket.html), newer NIO [DatagramChannel](http://docs.oracle.com/javase/7/docs/api/java/nio/channels/DatagramChannel.html) or even network frameworks like [Apache MINA](https://mina.apache.org/))

[Talk is cheap. Show me the code.](http://lkml.org/lkml/2000/8/25/132)
--------------------------------
Here is a minimal, complete example to show how straightforward it is to use JNetRobust:
* `DefaultHost` abstracts away much of the protocol's functionality with a default configuration, and sets-up a `DatagramChannel` for sending/receiving data, as well as a `Kryo` instance for serialization.
* If you need more flexibility, you can configure a `Protocol` instance instead and manage network communication & serialization yourself

```java
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
```
