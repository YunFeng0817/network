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
        do {
            while (timers.size() < WindowSize && sendIndex < content.length) { // until the window is run up
                timers.add(0);
                datagramBuffer.add(new ByteArrayOutputStream());
                length = content.length - sendIndex < MAX_LENGTH ? content.length - sendIndex : MAX_LENGTH;
                ByteArrayOutputStream oneSend = datagramBuffer.get(sendSeq - base);
                byte[] temp = new byte[1];
                temp[0] = new Integer(base).byteValue();
                oneSend.write(temp, 0, 1);
                temp = new byte[1];
                temp[0] = new Integer(sendSeq).byteValue();
                oneSend.write(temp, 0, 1);
                oneSend.write(content, sendIndex, length);
//                oneSend.write(new byte[0xe0f0], 0, 1);
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
            for (int i = 0; i < timers.size(); i++) { // update timer
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
        } while (sendIndex < content.length || timers.size() != 0); // until data has all transported
    }

    ByteArrayOutputStream receive() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        DatagramSocket datagramSocket = new DatagramSocket();
        List<ByteArrayOutputStream> datagramBuffer = new ArrayList<>(); // window buffer,used to resent the data
        DatagramPacket recvPacket;
        int time = 0;
        datagramSocket.setSoTimeout(1000);
        int max = 0;
        while (true) {
            try {
                byte[] recv = new byte[1500];
                recvPacket = new DatagramPacket(recv, recv.length);
                datagramSocket.receive(recvPacket);
                int base = new Byte(recv[0]).intValue();
                int seq = new Byte(recv[1]).intValue();
                if (seq - base > max) {
                    max = seq - max;
                }
                ByteArrayOutputStream recvBytes = new ByteArrayOutputStream();
                System.out.print(recvBytes.toString());
                recvBytes.write(recv, 2, recvPacket.getLength() - 2);
                datagramBuffer.set(seq - base, new ByteArrayOutputStream());
                // send ACK
                recv = new byte[1];
                recv[0] = (byte) seq;
                recvPacket = new DatagramPacket(recv, recv.length);
                datagramSocket.send(recvPacket);
            } catch (SocketTimeoutException e) {
                time++;
            }
            if (datagramBuffer.size() == WindowSize) {
                ByteArrayOutputStream temp = getBytes(datagramBuffer);
                result.write(temp.toByteArray(), 0, temp.size());
                datagramBuffer = new ArrayList<>();
            }
            if (time > this.MaxTime) {
                ByteArrayOutputStream temp = getBytes(datagramBuffer);
                result.write(temp.toByteArray(), 0, temp.size());
                break;
            }
        }
        return result;
    }

    ByteArrayOutputStream getBytes(List<ByteArrayOutputStream> buffer) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (ByteArrayOutputStream byteArray : buffer) {
            if (byteArray != null)
                result.write(byteArray.toByteArray(), 0, byteArray.size());
        }
        return result;
    }

    boolean checkWindow(List<Integer> timers) {
        for (int i = 0; i < timers.size(); i++) {
            if (timers.get(i) != -1)
                return false;
        }
        return true;
    }
}
