package Lab2;

import java.io.*;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new File("./src/Lab2/2.png");
        File file2 = new File("./src/Lab2/3.png");
        if (!file.exists()) {
            file.createNewFile();
        }
        SR server = new SR("localhost", 8080);
        while (true) {
            ByteArrayOutputStream byteArrayOutputStream = server.receive();
//            if (byteArrayOutputStream.size() != 0) {
//                System.out.println(byteArrayOutputStream.toString());
//                break;
//            }
            if (byteArrayOutputStream.size() != 0) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
                fileOutputStream.close();
                System.out.println("Get the file ");
                System.out.println("Saved as 2.png");
                fileOutputStream.close();
                break;
            }

            Thread.sleep(50);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Client.CloneStream(byteArrayOutputStream, new FileInputStream(file2));
        System.out.println("Start to send file 3.png to " + "localhost" + 8080);
        server.send(byteArrayOutputStream.toByteArray());
    }
}
