import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    public static void main(String[] args) throws IOException {
        int PORT = 8080;
        System.out.println("Listen to the port: " + PORT);
        ServerSocket server = new ServerSocket(PORT);
        Socket client;
        ByteArrayOutputStream ObtainClone;
        while (true) {
            client = server.accept();
            client.setSoTimeout(1000);
            System.out.println("Connected successfully");
            ObtainClone = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = client.getInputStream().read(buffer)) != -1) {
                    ObtainClone.write(buffer, 0, length);
                }
            } catch (SocketTimeoutException e) {

            }

            ObtainClone.flush();

            BufferedReader ObtainReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ObtainClone.toByteArray())));
            String line;
            String host = "";
            while (!((line = ObtainReader.readLine()) == null || line.isEmpty())) {
                Pattern HostPattern = Pattern.compile("Host:\\s*([\\w\\.]*)");
                Matcher matcher = HostPattern.matcher(line);
                if (matcher.find()) {
                    host = matcher.group(1);
                    System.out.println(host);
                    break;
                }
            }
            try {
                Socket ProxyClient = new Socket(host, 80);
                ProxyClient.setSoTimeout(200);
                ProxyClient.getOutputStream().write(ObtainClone.toByteArray());
                ObtainClone.close();
                ByteArrayOutputStream ServerCache = new ByteArrayOutputStream();
                try {
                    while ((length = ProxyClient.getInputStream().read(buffer)) != -1) {
                        ServerCache.write(buffer, 0, length);
                    }
                } catch (SocketTimeoutException e) {

                }
                ServerCache.flush();
                client.getOutputStream().write(ServerCache.toByteArray());
                ServerCache.close();
                client.close();
            } catch (Exception e) {

            }
        }
    }
}
