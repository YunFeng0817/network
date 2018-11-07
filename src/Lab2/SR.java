package Lab2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.LinkedList;

class SR {
    private InetAddress host;
    private int targetPort, ownPort;
    private int WindowSize = 16;
    private final int sendMaxTime = 2, receiveMaxTime = 4; // max time for one datagram
    private long base = 0;
    private final int virtualLossRemainder = 17; // this value is used to simulate the loss of the datagram as a remainder

    SR(String host, int targetPort, int ownPort) throws UnknownHostException {
        this.ownPort = ownPort;
        this.host = InetAddress.getByName(host);
        this.targetPort = targetPort;
    }

    /**
     * transport content data to host:targetPort
     *
     * @param content the content data to transport
     * @throws IOException
     */
    void send(byte[] content) throws IOException {
        int sendIndex = 0, length;
        final int MAX_LENGTH = 1024;
        DatagramSocket datagramSocket = new DatagramSocket(ownPort);
        List<ByteArrayOutputStream> datagramBuffer = new LinkedList<>(); // window buffer,used to resent the data
        List<Integer> timers = new LinkedList<>();
        long sendSeq = base;
        do {
            while (timers.size() < WindowSize && sendIndex < content.length && sendSeq < 256) { // until the window is run up
                timers.add(0);
                datagramBuffer.add(new ByteArrayOutputStream());
                length = content.length - sendIndex < MAX_LENGTH ? content.length - sendIndex : MAX_LENGTH;
                ByteArrayOutputStream oneSend = new ByteArrayOutputStream();
                byte[] temp = new byte[1];
                temp[0] = new Long(base).byteValue();
                oneSend.write(temp, 0, 1);
                temp = new byte[1];
                temp[0] = new Long(sendSeq).byteValue();
                oneSend.write(temp, 0, 1);
                oneSend.write(content, sendIndex, length);
                DatagramPacket datagramPacket = new DatagramPacket(oneSend.toByteArray(), oneSend.size(), host, targetPort);
                datagramSocket.send(datagramPacket);
                datagramBuffer.get((int) (sendSeq - base)).write(content, sendIndex, length);
                sendIndex += length;
                System.out.println("send the datagram : base " + base + " seq " + sendSeq);
                sendSeq++;
            }
            datagramSocket.setSoTimeout(1000);
            DatagramPacket receivePacket;
            try { // receive ACKs
                while (!checkWindow(timers)) {
                    byte[] recv = new byte[1500];
                    receivePacket = new DatagramPacket(recv, recv.length);
                    datagramSocket.receive(receivePacket);
                    int ack = (int) ((recv[0] & 0x0FF) - base);
                    timers.set(ack, -1);
                }
            } catch (SocketTimeoutException e) {  // out of time
                for (int i = 0; i < timers.size(); i++) {
                    if (timers.get(i) != -1)
                        timers.set(i, timers.get(i) + 1);
                }
            }
            for (int i = 0; i < timers.size(); i++) { // update timer
                if (timers.get(i) > this.sendMaxTime) { // resend the datagram which hasn't receive ACK and over time
                    ByteArrayOutputStream resender = new ByteArrayOutputStream();
                    byte[] temp = new byte[1];
                    temp[0] = new Long(base).byteValue();
                    resender.write(temp, 0, 1);
                    temp = new byte[1];
                    temp[0] = new Long(i + base).byteValue();
                    resender.write(temp, 0, 1);
                    resender.write(datagramBuffer.get(i).toByteArray(), 0, datagramBuffer.get(i).size());
                    DatagramPacket datagramPacket = new DatagramPacket(resender.toByteArray(), resender.size(), host, targetPort);
                    datagramSocket.send(datagramPacket);
                    System.err.println("resend the datagram : base " + base + " seq " + (i + base));
                    timers.set(i, 0);
                }
            }
            // slide the window if front datagram is acknowledged
            int i = 0, s = timers.size();
            while (i < s) {
                if (timers.get(i) == -1) {
                    timers.remove(i);
                    datagramBuffer.remove(i);
                    base++;
                    s--;
                } else {
                    break;
                }
            }
            if (base >= 256) {
                base = base - 256;
                sendSeq = sendSeq - 256;
            }
        } while (sendIndex < content.length || timers.size() != 0); // until data has all transported
        datagramSocket.close();
    }

    /**
     * receive data from host:targetPort
     *
     * @return the received data
     * @throws IOException IO exception occur
     */
    ByteArrayOutputStream receive() throws IOException {
        int count = 0, time = 0; // used to simulate datagram loss
        long max = 0, receiveBase = -1;
        ByteArrayOutputStream result = new ByteArrayOutputStream(); // store the received content
        DatagramSocket datagramSocket = new DatagramSocket(ownPort);   // UDP socket to receive datagram and send ACKs
        List<ByteArrayOutputStream> datagramBuffer = new LinkedList<>(); // window buffer,used to store the datagram out of order
        DatagramPacket receivePacket; // one temp datagram packet
        datagramSocket.setSoTimeout(1000);
        for (int i = 0; i < WindowSize; i++) {
            datagramBuffer.add(new ByteArrayOutputStream());
        }
        while (true) {
            // receive one datagram and send ACK
            try {
                byte[] recv = new byte[1500];
                receivePacket = new DatagramPacket(recv, recv.length, host, targetPort);
                datagramSocket.receive(receivePacket);
                // simulate datagram loss when count%virtualLossRemainder ==0
                if (count % virtualLossRemainder != 0) {
                    long base = recv[0] & 0x0FF;
                    long seq = recv[1] & 0x0FF;
                    if (receiveBase == -1)
                        receiveBase = base;
                    // slide the window
                    if (base != receiveBase) {
                        ByteArrayOutputStream temp = getBytes(datagramBuffer, (base - receiveBase) > 0 ? (base - receiveBase) : max + 1);
                        for (int i = 0; i < base - receiveBase; i++) {
                            datagramBuffer.remove(0);
                            datagramBuffer.add(new ByteArrayOutputStream());
                        }
                        result.write(temp.toByteArray(), 0, temp.size());
                        receiveBase = base;
                        max = max - (base - receiveBase);
                    }
                    if (seq - base > max) {
                        max = seq - base;
                    }
                    ByteArrayOutputStream recvBytes = new ByteArrayOutputStream();
                    recvBytes.write(recv, 2, receivePacket.getLength() - 2);
                    datagramBuffer.set((int) (seq - base), recvBytes);
                    // send ACK
                    recv = new byte[1];
                    recv[0] = new Long(seq).byteValue();
                    receivePacket = new DatagramPacket(recv, recv.length, host, targetPort);
                    datagramSocket.send(receivePacket);
                    System.out.println("receive datagram : base " + base + " seq " + seq);
                }
                count++;
                time = 0;
            } catch (SocketTimeoutException e) {
                time++;
            }
            if (time > receiveMaxTime) {  // check if the connect out of time
                ByteArrayOutputStream temp = getBytes(datagramBuffer, max + 1);
                result.write(temp.toByteArray(), 0, temp.size());
                break;
            }
        }
        datagramSocket.close();
        return result;
    }

    /**
     * splice the ByteArrays(datagram) to one ByteArray(one datagram)
     *
     * @param buffer the datagram in current window
     * @param max    the max datagram
     * @return spliced datagram(ByteArray)
     */
    private ByteArrayOutputStream getBytes(List<ByteArrayOutputStream> buffer, long max) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (int i = 0; i < max; i++) {
            if (buffer.get(i) != null)
                result.write(buffer.get(i).toByteArray(), 0, buffer.get(i).size());
        }
        return result;
    }

    /**
     * check if it's ok to slide window
     *
     * @param timers the timer to mark the window datagram
     * @return boolean  true-> it's ok to slide window ;false-> it can slide window
     */
    private boolean checkWindow(List<Integer> timers) {
        for (Integer timer : timers) {
            if (timer != -1)
                return false;
        }
        return true;
    }
}
