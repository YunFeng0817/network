package Lab2;

import java.io.ByteArrayOutputStream;
import java.io.*;
import java.io.IOException;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        File file1 = new File("./src/Lab2/1.png");
        File file2 = new File("./src/Lab2/4.png");
        if (!file2.exists()) {
            file2.createNewFile();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CloneStream(byteArrayOutputStream, new FileInputStream(file1));
        SR client = new SR("localhost", 7070, 8080);
        System.out.println("Start to send file 1.png to " + "localhost " + 7070);
        client.send(byteArrayOutputStream.toByteArray());
        System.out.println("\nStart to receive file 3.png from " + "localhost " + 7070);
        while (true) {
            byteArrayOutputStream = client.receive();
            if (byteArrayOutputStream.size() != 0) {
                FileOutputStream fileOutputStream = new FileOutputStream(file2);
                fileOutputStream.write(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
                fileOutputStream.close();
                System.out.println("Get the file ");
                System.out.println("Saved as 4.png");
                break;
            }

            Thread.sleep(50);
        }
    }

    /**
     * clone the input stream to a ByteArrayOutputStream object
     *
     * @param CloneResult the clone result of input stream
     * @param InputStream the input stream to be cloned
     * @throws IOException when read input stream, some exception occur
     */
    static void CloneStream(ByteArrayOutputStream CloneResult, InputStream InputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = InputStream.read(buffer)) != -1) {
            CloneResult.write(buffer, 0, length);
        }
        CloneResult.flush();
    }

}
