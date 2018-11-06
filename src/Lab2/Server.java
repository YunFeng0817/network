package Lab2;

import java.io.IOException;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
//        DatagramSocket datagramSocket = new DatagramSocket(8888);
//        byte[] test = new byte[1000];
//        DatagramPacket datagramPacket = new DatagramPacket(test, test.length);
//        datagramSocket.receive(datagramPacket);
//        System.out.println(new String(test, 0, datagramPacket.getLength()));
        while (true) {
            SR test = new SR(8080);
            System.out.print(test.receive().toString());
            Thread.sleep(50);
        }
    }
}
