/**
 * The MIT License (MIT)
 * Copyright (c) 2013 mucaho (http://mucaho.ddns.net/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.ddns.mucaho.jnetemu;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A convenience class for debugging UDP communication. The DatagramWanEmulator sits between
 * two specified {@link DatagramSocket}s and introduces latency, jitter and package loss 
 * between these two sockets.
 * <p>
 * In order to use the emulator, you send the {@link DatagramPacket}s directly 
 * to the emulator's {@link java.net.SocketAddress} instead of sending them to the receiver 
 * {@link java.net.SocketAddress}. 
 * The emulator will introduce latency, jitter and package loss. Then it will send 
 * the packets to the receiving socket.
 * <p>
 * This class can be instantiated with the respective addresses. Use the appropriate methods
 * to start and stop the emulation.
 * The specific emulation parameters can be tuned with the appropriate getters / setters. 
 *
 * @author mucaho
 *
 */
public class DatagramWanEmulator {
    /**
     * Internally used. Queues up delayed sending of datagram packets
     */
    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    /** The socket address A. */
    private final InetSocketAddress socketAddressA;

    /** The socket address B. */
    private InetSocketAddress socketAddressB;

    /** The emulator socket. */
    private DatagramSocket emulatorSocket;

    /**
     * Construct a new DatagramWanEmulator. This will automatically create a new socket
     * bound to the specified emulatorAddress. The socketAddressB will be captured the
     * first time a packet is sent from socketAddressB.
     *
     * @param emulatorAddress	the emulator address
     * @param socketAddressA	one socket address
     * @throws SocketException	the socket exception
     */
    public DatagramWanEmulator(InetSocketAddress emulatorAddress, InetSocketAddress socketAddressA)
            throws SocketException {
        this(emulatorAddress, socketAddressA, null);
    }

    /**
     * Construct a new DatagramWanEmulator. This will automatically create a new socket
     * bound to the specified emulatorAddress.
     *
     * @param emulatorAddress	the emulator address
     * @param socketAddressA	one socket address
     * @param socketAddressB	the other socket address
     * @throws SocketException	the socket exception
     */
    public DatagramWanEmulator(InetSocketAddress emulatorAddress, InetSocketAddress socketAddressA,
                               InetSocketAddress socketAddressB) throws SocketException {
        this.socketAddressA = socketAddressA;
        this.socketAddressB = socketAddressB;

        emulatorSocket = new DatagramSocket(emulatorAddress);
    }

    /**
     * The maximum size of incoming / outcoming datagram packets (in bytes).
     * Defaults to 1024.
     */
    private int maxPacketLength = 1024;
    /**
     * The amount of package loss. A value of 0.0f means no package loss,
     * a value of 1.0f means every packet will be lost.
     * Defaults to 0.1f (10%).
     */
    private volatile float packageLoss = 0.10f;

    /**
     * The chance of a packet being duplicated. A value of 0.0f means no
     * packets will be duplicated, a value of 1.0f means every packet will
     * be duplicated infinitely.
     * Defaults to 0.03f (3%).
     */
    private volatile float packageDuplication = 0.03f;
    /**
     * The maximum latency between sending from one host to receiving on the other one.
     * The latency will vary between minLatency and maxLatency.
     * Defaults to 250 ms.
     */
    private volatile int maxLatency = 250;

    /**
     * The minimum latency between sending from one host to receiving on the other one.
     * The latency will vary between minLatency and maxLatency.
     * Defaults to 100 ms.
     */
    private volatile int minLatency = 100;

    /**
     * Gets the maximum size of incoming / outcoming datagram packets (in bytes).
     * Defaults to 1024.
     *
     * @return the max packet length
     */
    public int getMaxPacketLength() {
        return maxPacketLength;
    }

    /**
     * Sets the maximum size of incoming / outcoming datagram packets (in bytes).
     * Defaults to 1024.
     *
     * @param maxPacketLength the new max packet length
     */
    public void setMaxPacketLength(int maxPacketLength) {
        this.maxPacketLength = maxPacketLength;
    }

    /**
     * Gets the amount of package loss.
     * A value of 0.0f means no package loss, a value of 1.0f means every packet will be lost.
     * Defaults to 0.1f (== 10%).
     *
     * @return the package loss percentage
     */
    public float getPackageLoss() {
        return packageLoss;
    }

    /**
     * Sets the amount of package loss.
     * A value of 0.0f means no package loss, a value of 1.0f means every packet will be lost.
     * Defaults to 0.1f (== 10%).
     *
     * @param percentage the new package loss percentage
     */
    public void setPackageLoss(float percentage) {
        this.packageLoss = percentage;
    }


    /**
     * Gets the chance of a packet being duplicated.
     * A value of 0.0f means no packets will be duplicated,
     * a value of 1.0f means every packet will be duplicated infinitely.
     * Defaults to 0.03f (3%).
     */
    public float getPackageDuplication() {
        return packageDuplication;
    }

    /**
     * Sets the chance of a packet being duplicated.
     * A value of 0.0f means no packets will be duplicated,
     * a value of 1.0f means every packet will be duplicated infinitely.
     * Defaults to 0.03f (3%).
     */
    public void setPackageDuplication(float packageDuplication) {
        this.packageDuplication = packageDuplication;
    }

    /**
     * Gets the maximum latency between sending from one host to receiving on the other one.
     * The latency will vary between minLatency and maxLatency. Defaults to 250 ms.
     *
     * @return the max latency
     */
    public int getMaxLatency() {
        return maxLatency;
    }

    /**
     * Sets the maximum latency between sending from one host to receiving on the other one.
     * The latency will vary between minLatency and maxLatency. Defaults to 250 ms.
     *
     * @param maxLatency the new max latency
     */
    public void setMaxLatency(int maxLatency) {
        this.maxLatency = maxLatency;
    }


    /**
     * Gets the minimum latency between sending from one host to receiving on the other one.
     * The latency will vary between minLatency and maxLatency. Defaults to 100 ms.
     *
     * @return the min latency
     */
    public int getMinLatency() {
        return minLatency;
    }

    /**
     * Sets the minimum latency between sending from one host to receiving on the other one.
     * The latency will vary between minLatency and maxLatency. Defaults to 100 ms.
     *
     * @param minLatency the new min latency
     */
    public void setMinLatency(int minLatency) {
        this.minLatency = minLatency;
    }

    /** The runnable. */
    private final Runnable runnable = new Runnable() {

        @Override
        public void run() { try {

            while(true) {
                byte[] bytes = new byte[maxPacketLength];
                final DatagramPacket packet = new DatagramPacket(bytes, maxPacketLength);
                emulatorSocket.receive(packet);

                if ((socketAddressB == null) &&
                        !(socketAddressA.equals(packet.getSocketAddress())))
                    socketAddressB = (InetSocketAddress) packet.getSocketAddress();

                if (socketAddressA.equals(packet.getSocketAddress()))
                    packet.setSocketAddress(socketAddressB);
                else if (socketAddressB.equals(packet.getSocketAddress()))
                    packet.setSocketAddress(socketAddressA);

                do {

                    if (Math.random() >= packageLoss)
                        executor.schedule(new Runnable() {

                                              @Override public void run() { try {
                                                  if (!emulatorSocket.isClosed())
                                                      emulatorSocket.send(packet);
                                              } catch(Exception e) {e.printStackTrace();}}

                                          }, minLatency + (int)(Math.random()* (maxLatency - minLatency)),
                                TimeUnit.MILLISECONDS);

                } while (Math.random() < packageDuplication);
            }


        } catch (SocketException se) {/*closed*/} catch (Exception e) { e.printStackTrace();}}
    };

    /**
     * Start the emulation between the two sockets.
     * @throws IllegalStateException if the current instance has already been stopped by stopEmulation().
     * Create new
     * {@link DatagramWanEmulator#DatagramWanEmulator(InetSocketAddress, InetSocketAddress, InetSocketAddress) instance}
     * instead.
     */
    public void startEmulation() throws IllegalStateException {
        if (emulatorSocket.isClosed())
            throw new IllegalStateException("This instance has already been stopped. Create a new one.");

        new Thread(runnable).start();
    }

    /**
     * Let the emulation end and close the associated emulator socket.
     */
    public void stopEmulation() {
        emulatorSocket.close();
    }
}
