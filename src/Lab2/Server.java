import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(8080);
        byte[] test = new byte[1000];
        DatagramPacket datagramPacket = new DatagramPacket(test, test.length);
        datagramSocket.receive(datagramPacket);
        System.out.println(Arrays.toString(datagramPacket.getData()));
    }
}
