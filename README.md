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

Why should I use it?
--------------------
If you only need validated data and you do not care about latency, you should rather use TCP.   
If you need validated data, but you could also benefit from receiving unvalidated data with no latency, JNetRobust could be for you.

How do you use it?
------------------
It is a library and imposes no restrictions on how you use it:   
* the protocol maintains an internal state, that is updated every time you use it
* the protocol just adds/removes metadata to/from your original data
* you decide what to do with the metadata-packaged data (e.g. add some of your own metadata, etc... )
* you decide how you want to serialize the metadata-packaged data (e.g. with [default serialization](http://docs.oracle.com/javase/7/docs/api/java/io/Externalizable.html), [Kryo](https://github.com/EsotericSoftware/kryo), etc... )
* you decide how you want to send the metadata-packaged data (e.g. plain [DatagramSocket](http://docs.oracle.com/javase/7/docs/api/java/net/DatagramSocket.html), newer NIO [DatagramChannel](http://docs.oracle.com/javase/7/docs/api/java/nio/channels/DatagramChannel.html) or even network libraries like [KryoNet](https://github.com/EsotericSoftware/kryonet))
