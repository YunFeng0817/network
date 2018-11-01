import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        int PORT = 8080;
        System.out.println("Listening to the port: " + PORT);
        ServerSocket server = new ServerSocket(PORT);
        Socket client;
        while (true) {
            client = server.accept();
            new Thread(new Client(client)).start();
        }
    }
}
