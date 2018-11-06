package Lab1;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HTTPSForward implements ProxyForward {
    @Override
    public void ProxyForward(Socket client, Socket server, long MaxWaitTime) {
        long LatestDataTransportTime = System.currentTimeMillis();
        ByteArrayOutputStream ClientCache; // used to cache the data from the client
        ByteArrayOutputStream ServerCache; // used to cache the data from the server
        while (server != null && !(client.isClosed() || server.isClosed())) {
            try {
                ClientCache = new ByteArrayOutputStream();
                try {
                    client.setSoTimeout(200);
                    Client.CloneStream(ClientCache, client.getInputStream());
                } catch (SocketTimeoutException e) {

                }
                server.getOutputStream().write(ClientCache.toByteArray());

                ServerCache = new ByteArrayOutputStream();
                try {
                    Client.CloneStream(ServerCache, server.getInputStream());
                } catch (SocketTimeoutException e) {

                }
                if (ClientCache.size() == 0 && ServerCache.size() == 0) {
                    // connection out of time, close the socket
                    if (System.currentTimeMillis() - LatestDataTransportTime > MaxWaitTime)
                        break;
                } else
                    LatestDataTransportTime = System.currentTimeMillis();
                client.getOutputStream().write(ServerCache.toByteArray());
                ClientCache.close();
                ServerCache.close();
            } catch (Exception e) {
                break;
            }
        }
    }
}
