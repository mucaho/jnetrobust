![jnetrobust](https://raw.githubusercontent.com/mucaho/jnetrobust/gh-pages/images/robust.png)
=============
[![Build Status](https://travis-ci.org/mucaho/jnetrobust.svg?branch=master)](https://travis-ci.org/mucaho/jnetrobust)

Fast, reliable &amp; non-intrusive message-oriented virtual network protocol for the JVM.   
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
   this is different from TCP, as TCP provides you the data only after it can guarantee in-order delivery
* the package is bigger than UDP's package, but smaller than TCP's package

**Caveats**
* no flow control - unlike TCP
* currently no congestion control - unlike TCP   
   [future releases](https://github.com/mucaho/jnetrobust/issues/11) may include this feature

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

Talk is cheap. Show me the code. [[1]](http://lkml.org/lkml/2000/8/25/132)
--------------------------------
Here is a minimal, complete example:   
**Code**
```java
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

        System.out.println();
        Thread.sleep(100);

        // receive at B
        receivedMessages = hostB.receive();
        for (String receivedMessage: receivedMessages)
            System.out.println("<B>\t"+receivedMessage);

        // send from B
        hostB.send("Howdy! Fine, thanks.");

        System.out.println();
        Thread.sleep(100);

        // receive at A
        receivedMessages = hostA.receive();
        for (String receivedMessage: receivedMessages)
            System.out.println("<A>\t"+receivedMessage);
    }
}
```
**Console Output**
```
 A	Data sent	                     -32767	      Hi!	 
 A	Data sent	                     -32766	      How you doing?	   

 B	Data received ordered	         -32767	      Hi!	  
 B	Data received	                 -32767	      Hi!	  
 B	Data received ordered	         -32766	      How you doing?	  
 B	Data received	                 -32766	      How you doing?
 <B>: Hi!  
 <B>: How you doing?   
 B	Data sent	                     -32767	      Howdy! Fine, thanks.	  

 A	Data was received at other end   -32767	      Hi!	  
 A	Data was received at other end	 -32766	      How you doing?	  
 A	Data received ordered	         -32767	      Howdy! Fine, thanks.	  
 A	Data received	                 -32767	      Howdy! Fine, thanks.	  
 <A>: Howdy! Fine, thanks.  
```

Misc
-------
__Open issues__ and/or __open pull requests__. Suggestions, bug reports, code improvements and additional features are very welcome!

Copyright (c) 2014 mucaho.   
[MPL 2.0](https://www.mozilla.org/MPL/2.0/). Read [short explanation](https://www.mozilla.org/MPL/2.0/FAQ.html#virality). I would suggest contributing to this original repository via pull requests, if you made any modifications.
