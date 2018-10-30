import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    public static void main(String[] args) throws IOException {
        int PORT = 8080;
        System.out.println("Listen to the port: " + PORT);
        ServerSocket server = new ServerSocket(PORT);
        Socket client;
        client = server.accept();
        System.out.println("Connected successfully");
        BufferedReader ObtainReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String header;
        String url;
        String method;
        header = ObtainReader.readLine();
        if (!header.isEmpty()) {
            String[] ParseResult = header.split(" ");
            method = ParseResult[0];
            url = ParseResult[1];

//
        }
        String line;
        String host = "";
        while (!(line = ObtainReader.readLine()).isEmpty()) {
            Pattern HostPattern = Pattern.compile("Host:\\s*([\\w\\.]*)");
            Matcher matcher = HostPattern.matcher(line);
            if (matcher.find()) {
                host = matcher.group(1).toString();
                break;
            }
        }
        InetAddress IPAdress = InetAddress.getByName(host);
        Socket ProxyClient = new Socket(IPAdress, 80);
        ProxyClient.setSoTimeout(3000);
        PrintStream Sender = new PrintStream(ProxyClient.getOutputStream());
        BufferedReader Response = new BufferedReader(new InputStreamReader(ProxyClient.getInputStream()));
        Sender.print(ObtainReader);
        while (!(line = Response.readLine()).isEmpty()) {
            System.out.println(line);
        }
    }
}
