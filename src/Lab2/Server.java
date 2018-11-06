package Lab2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
//        DatagramSocket datagramSocket = new DatagramSocket(8888);
//        byte[] test = new byte[1000];
//        DatagramPacket datagramPacket = new DatagramPacket(test, test.length);
//        datagramSocket.receive(datagramPacket);
//        System.out.println(new String(test, 0, datagramPacket.getLength()));
        File file = new File("./src/Lab2/2.png");
        if (!file.exists()) {
            file.createNewFile();
        }
        while (true) {
            SR test = new SR(8080);
            ByteArrayOutputStream byteArrayOutputStream;
            byteArrayOutputStream = test.receive();
            if (byteArrayOutputStream.size() != 0) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
                fileOutputStream.close();
                break;
            }
            Thread.sleep(50);
        }
    }
}
