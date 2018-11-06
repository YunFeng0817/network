package Lab1;

import java.io.*;
import java.net.Socket;

public class PhishForward implements ProxyForward {
    @Override
    public void ProxyForward(Socket client, Socket server, long MaxWaitTime) {
        File PhishFile = new File("./src/Lab1/phish.html");
        ByteArrayOutputStream PhishCache = new ByteArrayOutputStream();
        try {
            if (server != null && !server.isClosed())
                server.close();
            Client.CloneStream(PhishCache, new FileInputStream(PhishFile));
            String PhishConent = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nConnection: close\r\n\r\n" + PhishCache.toString("UTF-8");
            client.getOutputStream().write(PhishConent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
