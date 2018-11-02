import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client implements Runnable {
    private Socket Client;
    private Socket ProxyClient;
    private String Host;
    private long LatestDataTransportTime; // The latest time when socket has data transport
    private final long aliveTime = 30000;
    private FireWall FileWall;

    Client(Socket Client, FireWall fireWall) {
        this.Client = Client;
        this.ProxyClient = null;
        this.Host = "";
        this.FileWall = fireWall;
    }

    @Override
    public void run() {
        ByteArrayOutputStream ClientCache = new ByteArrayOutputStream(); // used to cache the data from the client
        ByteArrayOutputStream ServerCache; // used to cache the data from the server
        int HTTPPort = 80;
        int HTTPSPort = 443;
        try {
            Client.setSoTimeout(200);
            CloneStream(ClientCache, Client.getInputStream());
        } catch (SocketTimeoutException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader ObtainReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ClientCache.toByteArray())));
        String line;
        // parse the http or https header
        try {
            while (!((line = ObtainReader.readLine()) == null || line.isEmpty())) {
                Pattern HostPattern = Pattern.compile("Host:\\s*([\\w.-]*)");
                Pattern ConnectPattern = Pattern.compile("CONNECT \\s*([\\w.-]*):443.*");
                Matcher HostMatcher = HostPattern.matcher(line);
                Matcher ConnectMatcher = ConnectPattern.matcher(line);
                if (ConnectMatcher.find()) {  // connect to HTTPS
                    this.Host = ConnectMatcher.group(1);
                    System.out.println("HTTPS request to Host: " + Host);
                    Client.getOutputStream().write("HTTP/1.1 200 Connection established\r\n\r\n".getBytes());
                    this.ProxyClient = new Socket(this.Host, HTTPSPort);
                    break;
                }
                if (HostMatcher.find()) { // connect to HTTP
                    this.Host = HostMatcher.group(1);
                    System.out.println("HTTP request to Host: " + Host);
                    this.ProxyClient = new Socket(this.Host, HTTPPort);
                    ProxyClient.getOutputStream().write(ClientCache.toByteArray());
                    break;
                }
            }
            if (ProxyClient != null)
                ProxyClient.setSoTimeout(200);
        } catch (ConnectException e) {
            if (e.getMessage().equals("Connection timed out: connect")) {
                System.err.println("Connect to Host: " + this.Host + " time out");
            } else if (e.getMessage().equals("Connection refused: connect")) {
                System.err.println("Connect to Host: " + this.Host + " refused");
            } else
                e.printStackTrace();
            CloseAllConnect();

        } catch (UnknownHostException e) {
            System.err.println("Unknown host name: " + e.getMessage());
            CloseAllConnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (FileWall.isForbidden(this.Host))
            CloseAllConnect();
        // transport data from server to client
        while (!(Client.isClosed() || ProxyClient.isClosed())) {
            try {
                ClientCache = new ByteArrayOutputStream();
                try {
                    Client.setSoTimeout(200);
                    CloneStream(ClientCache, Client.getInputStream());
                } catch (SocketTimeoutException e) {

                }
                ProxyClient.getOutputStream().write(ClientCache.toByteArray());

                ServerCache = new ByteArrayOutputStream();
                try {
                    CloneStream(ServerCache, ProxyClient.getInputStream());
                } catch (SocketTimeoutException e) {

                }
                if (ClientCache.size() == 0 && ServerCache.size() == 0) {
                    // connection out of time, close the socket
                    if (System.currentTimeMillis() - LatestDataTransportTime > aliveTime)
                        break;
                } else
                    LatestDataTransportTime = System.currentTimeMillis();
                Client.getOutputStream().write(ServerCache.toByteArray());
                ClientCache.close();
                ServerCache.close();
            } catch (Exception e) {
                break;
            }
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
    private static void CloneStream(ByteArrayOutputStream CloneResult, InputStream InputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = InputStream.read(buffer)) != -1) {
            CloneResult.write(buffer, 0, length);
        }
        CloneResult.flush();
    }
}
