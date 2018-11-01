import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client implements Runnable {
    private Socket Client;
    private Socket ProxyClient;
    private String Host;

    Client(Socket Client) {
        this.Client = Client;
        this.ProxyClient = null;
        this.Host = "";
    }

    @Override
    public void run() {

        ByteArrayOutputStream ObtainClone;
        while (true) {
            try {
                Client.setSoTimeout(200);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            ObtainClone = new ByteArrayOutputStream();
            try {
                CloneStream(ObtainClone, Client.getInputStream());
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (ObtainClone.size() != 0) {
                if (Host.equals("")) {
                    BufferedReader ObtainReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ObtainClone.toByteArray())));
                    String line;
                    try {
                        while (!((line = ObtainReader.readLine()) == null || line.isEmpty())) {
                            Pattern HostPattern = Pattern.compile("Host:\\s*([\\w.-]*)");
                            Pattern ConnectPattern = Pattern.compile("CONNECT \\s*([\\w.-]*):443.*");
                            Matcher HostMatcher = HostPattern.matcher(line);
                            Matcher ConnectMatcher = ConnectPattern.matcher(line);
                            if (ConnectMatcher.find()) {
                                this.Host = ConnectMatcher.group(1);
                                System.out.println("HTTPS request to " + Host);
                                OutputStream ClientOutput = Client.getOutputStream();
                                ClientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                                ClientOutput.flush();
                                HTTPSProxy();
                                break;
                            }
                            if (HostMatcher.find()) {
                                this.Host = HostMatcher.group(1);
                                System.out.println(line);
                                System.out.println("HTTP request to " + Host);
                                HTTPProxy();
                                break;
                            }
                        }
                        if (this.ProxyClient == null)
                            continue;
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                try {
                    ProxyClient.setSoTimeout(200);
                    ProxyClient.getOutputStream().write(ObtainClone.toByteArray());
                    ObtainClone.close();
                    ByteArrayOutputStream ServerCache = new ByteArrayOutputStream();
                    try {
                        CloneStream(ServerCache, ProxyClient.getInputStream());
                    } catch (SocketTimeoutException e) {

                    }
                    ServerCache.flush();
                    Client.getOutputStream().write(ServerCache.toByteArray());
                    ServerCache.close();
                } catch (Exception e) {

                }
                if (Client.isClosed())
                    break;
            }
        }
        try {
            Client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void CloneStream(ByteArrayOutputStream CloneResult, InputStream InputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = InputStream.read(buffer)) != -1) {
            CloneResult.write(buffer, 0, length);
        }
        CloneResult.flush();
    }

    private void HTTPProxy() throws IOException {
        this.ProxyClient = new Socket(this.Host, 80);
    }

    private void HTTPSProxy() throws IOException {
        this.ProxyClient = new Socket(this.Host, 443);
    }
}
