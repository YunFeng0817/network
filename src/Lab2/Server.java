package Lab2;

import java.io.*;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        File file1 = new File("./src/Lab2/2.png");
        File file2 = new File("./src/Lab2/3.png");
        if (!file1.exists()) {
            file1.createNewFile();
        }
        SR server = new SR("localhost", 8080, 7070);
        System.out.println("Start to receive file 1.png from " + "localhost " + 8080);
        while (true) {
            ByteArrayOutputStream byteArrayOutputStream = server.receive();
            if (byteArrayOutputStream.size() != 0) {
                FileOutputStream fileOutputStream = new FileOutputStream(file1);
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
        System.out.println("\nStart to send file 3.png to " + "localhost" + 8080);
        server.send(byteArrayOutputStream.toByteArray());
    }
}
