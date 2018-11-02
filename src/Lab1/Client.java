import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Client implements Runnable {
    private Socket Client;
    private Socket ProxyClient;
    private ProtocolHeader header;
    private final long aliveTime = 10000;
    private FireWall FileWall;

    Client(Socket Client, FireWall fireWall) {
        this.Client = Client;
        this.ProxyClient = null;
        this.FileWall = fireWall;
    }

    @Override
    public void run() {
        ByteArrayOutputStream ClientCache = new ByteArrayOutputStream(); // used to cache the data from the client
        try {
            Client.setSoTimeout(200);
            CloneStream(ClientCache, Client.getInputStream());
        } catch (SocketTimeoutException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader ObtainReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ClientCache.toByteArray())));
        // parse the http or https header
        try {
            header = new ProtocolHeader(ObtainReader);
            // filter some host by fire wall
            if (!FileWall.isHostForbidden(header.getHost())) {
                this.ProxyClient = new Socket(header.getHost(), header.getPort());
                if (header.getPort() == 80) {
                    System.out.println("HTTP request to Host: " + header.getHost());
                    ProxyClient.getOutputStream().write(ClientCache.toByteArray());
                } else if (header.getPort() == 443) {
                    System.out.println("HTTPS request to Host: " + header.getHost());
                    Client.getOutputStream().write("HTTP/1.1 200 Connection established\r\n\r\n".getBytes());
                }
            } else {
                System.err.println("Forbid the destiny host: " + header.getHost());
            }
            if (ProxyClient != null)
                ProxyClient.setSoTimeout(200);
        } catch (ConnectException e) {
            if (e.getMessage().equals("Connection timed out: connect")) {
                System.err.println("Connect to Host: " + header.getHost() + " time out");
            } else if (e.getMessage().equals("Connection refused: connect")) {
                System.err.println("Connect to Host: " + header.getHost() + " refused");
            } else
                e.printStackTrace();
            CloseAllConnect();

        } catch (UnknownHostException e) {
            System.err.println("Unknown host name: " + e.getMessage());
            CloseAllConnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // transport data from server to client
        ProxyForward forward;
        switch (header.getPort()) {
            case 80:
                forward = new HTTPForward();
                forward.ProxyForward(Client, ProxyClient, aliveTime);
            case 443:
                forward = new HTTPSForward();
                forward.ProxyForward(Client, ProxyClient, aliveTime);
        }
        CloseAllConnect();
    }

    /**
     * used to close all socket contain the Client and ProxyClient
     */
    private void CloseAllConnect() {
        try {
            if (Client != null && !Client.isClosed())
                Client.close();
            if (ProxyClient != null && !ProxyClient.isClosed())
                ProxyClient.close();
        } catch (IOException e) {
            e.printStackTrace();
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
