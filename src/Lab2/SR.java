package Lab2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.LinkedList;

public class SR {
    private InetAddress host;
    private int port;
    private int WindowSize = 16;
    private final int MaxTime = 5; // max time for one datagram
    private List<ByteArrayOutputStream> buffer = new LinkedList<>();
    private int base = 0;

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
        int sendSeq = base;
        while (sendIndex < content.length) { // until data has all transported
            while (timers.size() < WindowSize && sendIndex < content.length) { // until the window is run up
                timers.add(0);
                datagramBuffer.add(new ByteArrayOutputStream());
                length = content.length - sendIndex < MAX_LENGTH ? content.length - sendIndex : MAX_LENGTH;
                ByteArrayOutputStream oneSend = datagramBuffer.get(sendSeq - base);
                oneSend.write(new byte[sendSeq], 0, 1);
                oneSend.write(content, sendIndex, length);
                oneSend.write(new byte[0xe0f0], 0, 1);
                sendIndex += length;
                DatagramPacket datagramPacket = new DatagramPacket(oneSend.toByteArray(), length, host, port);
                datagramSocket.send(datagramPacket);
                sendSeq++;
            }
            datagramSocket.setSoTimeout(1000);
            DatagramPacket recvPacket;
            try { // receive ACKs
                while (!checkWindow(timers)) {
                    byte[] recv = new byte[1500];
                    recvPacket = new DatagramPacket(recv, recv.length);
                    datagramSocket.receive(recvPacket);
                    int ack = new Byte(recv[0]).intValue();
                    timers.set(ack, -1);
                }
            } catch (SocketTimeoutException e) {  // out of time
                for (int i = 0; i < timers.size(); i++) {
                    if (timers.get(i) != -1)
                        timers.set(i, timers.get(i) + 1);
                }
            }
            for (int i = 0; i < WindowSize; i++) { // update timer
                if (timers.get(i) > this.MaxTime) {
                    ByteArrayOutputStream temp = datagramBuffer.get(i);
                    DatagramPacket datagramPacket = new DatagramPacket(temp.toByteArray(), temp.size(), host, port);
                    datagramSocket.send(datagramPacket);
                    timers.set(i, 0);
                }
            }
            // slide the window if front datagram is acknowledged
            int i = 0, s = timers.size();
            while (i < s) {
                if (timers.get(i) == -1) {
                    timers.remove(i);
                    i--;
                    s--;
                } else {
                    break;
                }
            }
        }
    }

    void receive() throws SocketException {
        DatagramSocket datagramSocket = new DatagramSocket();
        List<ByteArrayOutputStream> datagramBuffer = new LinkedList<>(); // window buffer,used to resent the data
        List<Integer> timers = new LinkedList<>();
    }

    boolean checkWindow(List<Integer> timers) {
        for (int i = 0; i < timers.size(); i++) {
            if (timers.get(i) != -1)
                return false;
        }
        return true;
    }
}
