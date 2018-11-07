package Lab2;

import java.io.ByteArrayOutputStream;
import java.io.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {
//        System.out.println("Please input destiny server host :");
//        Scanner in = new Scanner(System.in);
//        String host = in.nextLine();
//        System.out.println("Please input destiny server port :");
//        int port = in.nextInt();
        File file = new File("./src/Lab2/1.png");
//        File files = new File("./src/Lab2/3.png");
//        if (!files.exists()) {
//            files.createNewFile();
//        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CloneStream(byteArrayOutputStream, new FileInputStream(file));
//        SR client = new SR(host, port);
        SR client = new SR("localhost", 8080);
//        System.out.println("Input instruction :");
//        String instruction = in.nextLine();
//        client.send(instruction.getBytes());
        client.send(byteArrayOutputStream.toByteArray());
//        FileOutputStream fileOutputStream = new FileOutputStream(files);
//        fileOutputStream.write(test.all.toByteArray(), 0, test.all.size());
//        fileOutputStream.close();
    }

    /**
     * clone the input stream to a ByteArrayOutputStream object
     *
     * @param CloneResult the clone result of input stream
     * @param InputStream the input stream to be cloned
     * @throws IOException when read input stream, some exception occur
     */
    private static void CloneStream(ByteArrayOutputStream CloneResult, InputStream InputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = InputStream.read(buffer)) != -1) {
            CloneResult.write(buffer, 0, length);
        }
        CloneResult.flush();
    }

}
