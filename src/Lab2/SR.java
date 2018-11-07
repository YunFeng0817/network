package Lab2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.LinkedList;

class SR {
    private InetAddress host;
    private int port;
    private int WindowSize = 16;
    private final int MaxTime = 2; // max time for one datagram
    private long base = 0;

    SR(String host, int port) throws UnknownHostException {
        this.host = InetAddress.getByName(host);
        this.port = port;
    }

    SR(int port) {
        this.port = port;
    }

    void send(byte[] content) throws IOException {
        int sendIndex = 0, length;
        final int MAX_LENGTH = 1024;
        DatagramSocket datagramSocket = new DatagramSocket();
        List<ByteArrayOutputStream> datagramBuffer = new LinkedList<>(); // window buffer,used to resent the data
        List<Integer> timers = new LinkedList<>();
        long sendSeq = base;
        do {
            while (timers.size() < WindowSize && sendIndex < content.length) { // until the window is run up
                timers.add(0);
                datagramBuffer.add(new ByteArrayOutputStream());
                length = content.length - sendIndex < MAX_LENGTH ? content.length - sendIndex : MAX_LENGTH;
                ByteArrayOutputStream oneSend = datagramBuffer.get((int) (sendSeq - base));
                byte[] temp = new byte[1];
                temp[0] = new Long(base).byteValue();
                oneSend.write(temp, 0, 1);
                temp = new byte[1];
                temp[0] = new Long(sendSeq).byteValue();
                oneSend.write(temp, 0, 1);
                oneSend.write(content, sendIndex, length);
                sendIndex += length;
                DatagramPacket datagramPacket = new DatagramPacket(oneSend.toByteArray(), length + 2, host, port);
                datagramSocket.send(datagramPacket);
                System.out.println("base " + base + " seq" + sendSeq);
                sendSeq++;
            }
            datagramSocket.setSoTimeout(1000);
            DatagramPacket recvPacket;
            try { // receive ACKs
                while (!checkWindow(timers)) {
                    byte[] recv = new byte[1500];
                    recvPacket = new DatagramPacket(recv, recv.length);
                    datagramSocket.receive(recvPacket);
                    int ack = (int) ((recv[0] & 0x0FF) - base);
//                    System.out.println("seq" + (recv[0] & 0x0FF));
//                    System.out.println("base" + base);
                    System.out.println(ack);
                    timers.set(ack, -1);
                }
            } catch (SocketTimeoutException e) {  // out of time
                for (int i = 0; i < timers.size(); i++) {
                    if (timers.get(i) != -1)
                        timers.set(i, timers.get(i) + 1);
                }
            }
            for (int i = 0; i < timers.size(); i++) { // update timer
                if (timers.get(i) > this.MaxTime) { // resend the datagram which hasn't receive ACK and over time
                    ByteArrayOutputStream temp = datagramBuffer.get(i);
                    DatagramPacket datagramPacket = new DatagramPacket(temp.toByteArray(), temp.size(), host, port);
                    datagramSocket.send(datagramPacket);
                    System.err.println("Resend the datagram : " + (i + base));
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
            if (base == 256) {
                base = 0;
                sendSeq = 0;
            }
        } while (sendIndex < content.length || timers.size() != 0); // until data has all transported
        datagramSocket.close();
    }

    ByteArrayOutputStream receive() throws IOException {
        int count = 0; // used to simulate datagram loss
        ByteArrayOutputStream result = new ByteArrayOutputStream(); // store the received content
        DatagramSocket datagramSocket = new DatagramSocket(port);   // UDP socket to receive datagram and send ACKs
        List<ByteArrayOutputStream> datagramBuffer = new ArrayList<>(); // window buffer,used to store the datagram out of order
        DatagramPacket recvPacket; // one temp datagram packet
        List<Integer> timers = new ArrayList<>(); // to mark the datagram's status, 0 -> hasn't not received  -1->obtained
        int time = 0;
        datagramSocket.setSoTimeout(1000);
        long max = 0;
        for (int i = 0; i < WindowSize; i++) {
            datagramBuffer.add(new ByteArrayOutputStream());
            timers.add(0);
        }
        while (true) {
            // receive one datagram and send ACK
            try {
                byte[] recv = new byte[1500];
                recvPacket = new DatagramPacket(recv, recv.length);
                datagramSocket.receive(recvPacket);
                if (count % 70 != 0) {
                    long base = recv[0] & 0x0FF;
                    long seq = recv[1] & 0x0FF;
                    if (seq - base > max) {
                        max = seq - base;
                    }
                    System.out.println("i  " + (seq - base));
                    System.out.println("timer  " + timers.get((int) (seq - base)));
                    if (timers.get((int) (seq - base)) == 0) {
                        System.out.println(seq);
                        ByteArrayOutputStream recvBytes = new ByteArrayOutputStream();
                        recvBytes.write(recv, 2, recvPacket.getLength() - 2);
                        datagramBuffer.set((int) (seq - base), recvBytes);
                        // send ACK
                        recv = new byte[1];
                        recv[0] = new Long(seq).byteValue();
                        recvPacket = new DatagramPacket(recv, recv.length, recvPacket.getAddress(), recvPacket.getPort());
                        datagramSocket.send(recvPacket);
                        timers.set((int) (seq - base), -1);
                    }
                }
                count++;
                time = 0;
            } catch (SocketTimeoutException e) {
                time++;
            }
            if (checkWindow(timers)) { // check if it's ok to slide window
                System.out.println("slide");
                ByteArrayOutputStream temp = getBytes(datagramBuffer, WindowSize);
                result.write(temp.toByteArray(), 0, temp.size());
                max = 0;
                datagramBuffer = new ArrayList<>();
                timers = new ArrayList<>();
                for (int i = 0; i < WindowSize; i++) {
                    datagramBuffer.add(new ByteArrayOutputStream());
                    timers.add(0);
                }
            }
            if (time > 10) {  // check if the connect out of time
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
