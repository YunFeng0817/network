package Lab2;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {
    private InetAddress Server;
    private int port;

    Client(String host, int Port) throws UnknownHostException {
        this.Server = InetAddress.getByName(host);
        this.port = Port;
    }

    List<String> getList() throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        byte[] sendBytes;
        sendBytes = "Get List".getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(sendBytes, sendBytes.length, Server, port);
        datagramSocket.send(datagramPacket);
        byte[] receiveBytes = new byte[1024];
        datagramPacket = new DatagramPacket(receiveBytes, receiveBytes.length, Server, port);
        datagramSocket.receive(datagramPacket);
        System.out.println(Arrays.toString(datagramPacket.getData()));
        return new ArrayList<>();
    }

    public static void main(String[] args) throws IOException {
//        System.out.println("Please input destiny server host :");
//        Scanner in = new Scanner(System.in);
//        String host = in.nextLine();
//        System.out.println("Please input destiny server port :");
//        int port = in.nextInt();
//        Client client = new Client(host, port);
//        client.getList();
//        SR test = new SR(host, port);
        SR test = new SR("localhost", 8080);
        test.send("testtsettesttesttest".getBytes());
    }

}
