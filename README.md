![jnetrobust](https://raw.githubusercontent.com/mucaho/jnetrobust/gh-pages/images/robust.png)
=============
[![Build Status](https://travis-ci.org/mucaho/jnetrobust.svg?branch=master)](https://travis-ci.org/mucaho/jnetrobust)
[![Coverage Status](https://img.shields.io/coveralls/mucaho/jnetrobust.svg)](https://coveralls.io/r/mucaho/jnetrobust)
[![Code Analysis Status](https://scan.coverity.com/projects/3328/badge.svg)](https://scan.coverity.com/projects/3328)
[![Library Dependency Status](https://www.versioneye.com/user/projects/544c117151259293d3000002/badge.svg)](https://www.versioneye.com/user/projects/544c117151259293d3000002)   
[![Maven Central](http://img.shields.io/maven-central/v/com.github.mucaho/jnetrobust.svg)](http://search.maven.org/#search|ga|1|jnetrobust*)
[![Supported Platform](http://img.shields.io/badge/java-1.6+-blue.svg)](http://docs.oracle.com/javase/6/docs/api/)
[![License](http://img.shields.io/badge/license-MPL-blue.svg)](https://www.mozilla.org/MPL/2.0/)


Fast, reliable & non-intrusive message-oriented virtual network protocol for Java 6+.   
**Currently in alpha.**

What is it?
-----------
JNetRobust is a virtual network protocol that resides between the [transport](http://en.wikipedia.org/wiki/Transport_layer) and the [application](http://en.wikipedia.org/wiki/Application_layer) layer.

It provides some benefits from both transport layer protocols that are accessible on the JVM:
* performance of [UPD](http://en.wikipedia.org/wiki/User_Datagram_Protocol)
* reliability of [TCP](http://en.wikipedia.org/wiki/Transmission_Control_Protocol)

**Benefits**
* reliability of transmitted data   
   counters network characteristics like out-of-order delivery, package loss, package duplication
* received, unvalidated data is available immediately   
   this is different from TCP, as TCP provides you the data only after it can guarantee in-order delivery
* the package is bigger than UDP's package, but smaller than TCP's package
* avoids some pitfalls of using UDP & TCP together   
   simultaneous UDP & TCP traffic [may lead to increased packet loss](http://www.isoc.org/INET97/proceedings/F3/F3_1.HTM)

**Caveats**
* no flow control
* currently no congestion control   
   [future releases](https://github.com/mucaho/jnetrobust/issues/11) may include this feature

Why should I use it?
----------------------
If you don't need reliability, use UDP.   
If you need reliability and you can wait for it to be validated, use TCP.   
If you need reliability, but you benefit from receiving unvalidated data with no latency, try JNetRobust.

How do I use it?
------------------
It is a library and imposes no restrictions on how you use it:   
* the protocol maintains an internal state, that is updated every time you use it
* the protocol just adds/removes metadata to/from your original data
* you decide what to do with the metadata-packaged data (e.g. add some of your own metadata, etc... )
* you decide how you want to serialize the metadata-packaged data (e.g. with [default serialization](http://docs.oracle.com/javase/7/docs/api/java/io/Externalizable.html), [Kryo](https://github.com/EsotericSoftware/kryo), etc... )
* you decide how you want to send the metadata-packaged data (e.g. plain [DatagramSocket](http://docs.oracle.com/javase/7/docs/api/java/net/DatagramSocket.html), newer NIO [DatagramChannel](http://docs.oracle.com/javase/7/docs/api/java/nio/channels/DatagramChannel.html) or even network frameworks like [Apache MINA](https://mina.apache.org/))

**Refer to the [Wiki pages](https://github.com/mucaho/jnetrobust/wiki) for additional information!**

Talk is cheap. Show me the code. [[1]](http://lkml.org/lkml/2000/8/25/132)
--------------------------------
Here is a minimal, complete example:   
**Code**
```java
public class BidirectionalMain {
    public static void main (String[] args) throws Exception {
        String receivedMessage;

        // host addresses
        InetSocketAddress ADDRESS_A = new InetSocketAddress(InetAddress.getLocalHost(), 12345);
        InetSocketAddress ADDRESS_B = new InetSocketAddress(InetAddress.getLocalHost(), 12346);

        // setup ProtocolHost A
        ProtocolHost<String> protocolHostA = new ProtocolHost<String>("A", String.class, ADDRESS_A);
        ProtocolHost.ProtocolHandle<String> protocolHandleA = protocolHostA.register(Byte.MIN_VALUE, ADDRESS_B);

        // setup ProtocolHost B
        ProtocolHost<String> protocolHostB = new ProtocolHost<String>("B", String.class, ADDRESS_B);
        ProtocolHost.ProtocolHandle<String> protocolHandleB = protocolHostB.register(Byte.MIN_VALUE, ADDRESS_A);


        // send from A
        protocolHandleA.send("Hi!");
        protocolHandleA.send("How you doing?");

        System.out.println();
        Thread.sleep(100);

        // receive at B
        while ((receivedMessage = protocolHandleB.receive()) != null) {
            System.out.println("<B>\t"+receivedMessage);
        }

        // send from B
        protocolHandleB.send("Howdy! Fine, thanks.");

        System.out.println();
        Thread.sleep(100);

        // receive at A
        while ((receivedMessage = protocolHandleA.receive()) != null) {
            System.out.println("<A>\t"+receivedMessage);
        }
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

Where can I download the binaries, sources and javadoc?
-------------------------------------------------------
**Maven**   
```xml
<!-- JNetRobust library; mandatory -->
<dependency>
    <groupId>com.github.mucaho</groupId>
    <artifactId>jnetrobust-core</artifactId>
    <version>0.0.2</version>
</dependency>
```
```xml
<!-- JNetRobust examples; optional -->
<dependency>
    <groupId>com.github.mucaho</groupId>
    <artifactId>jnetrobust-samples</artifactId>
    <version>0.0.2</version>
</dependency>
```

**Manual setup**   
You can download the `jar`s from the [release section](https://github.com/mucaho/jnetrobust/releases) and import them to your classpath.

Where can I browse the javadoc?
------------------------------
You can read the docs on [JNetRobusts's IO page](http://mucaho.github.io/jnetrobust/).

Misc
-------
__Open issues__ and/or __open pull requests__. Suggestions, bug reports, code improvements and additional features are very welcome!

Copyright (c) 2014 mucaho.   
[MPL 2.0](https://www.mozilla.org/MPL/2.0/). Read [short explanation](https://www.mozilla.org/MPL/2.0/FAQ.html#virality). I would like to encourage contributing to this original repository via pull requests, if you made any modifications.
