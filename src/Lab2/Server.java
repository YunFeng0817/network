package Lab2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new File("./src/Lab2/2.png");
        if (!file.exists()) {
            file.createNewFile();
        }
        while (true) {
            SR server = new SR(8080);
            ByteArrayOutputStream byteArrayOutputStream = server.receive();
//            if (byteArrayOutputStream.size() != 0) {
//                System.out.println(byteArrayOutputStream.toString());
//                break;
//            }
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
