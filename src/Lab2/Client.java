package Lab2;

import java.io.ByteArrayOutputStream;
import java.io.*;
import java.io.IOException;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new File("./src/Lab2/1.png");
        File files = new File("./src/Lab2/4.png");
        if (!files.exists()) {
            files.createNewFile();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CloneStream(byteArrayOutputStream, new FileInputStream(file));
        SR client = new SR("localhost", 8080);
        System.out.println("Start to send file 1.png to " + "localhost" + 8080);
        client.send(byteArrayOutputStream.toByteArray());
        Thread.sleep(6000);
        while (true) {
            byteArrayOutputStream = client.receive();
            if (byteArrayOutputStream.size() != 0) {
                FileOutputStream fileOutputStream = new FileOutputStream(files);
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
