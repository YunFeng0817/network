package Lab1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        final String FILE = "./src/Lab1/config.json";
        FireWall FireWall = new FireWall(FILE);
        int PORT = 8080;
        System.out.println("Listening to the port: " + PORT);
        ServerSocket server = new ServerSocket(PORT);
        Socket client;
        while (true) {
            client = server.accept();
            if (!FireWall.isClientForbidden(client.getInetAddress().getHostName()))
                new Thread(new Client(client, FireWall)).start();
            else {
                client.close();
                System.err.println("Forbid the client host: " + client.getInetAddress().getHostName());
            }
        }
    }
}
