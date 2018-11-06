package Lab1;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProtocolHeader {
    private String Host;
    private int port;

    ProtocolHeader(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            Pattern HostPattern = Pattern.compile("Host:\\s*([\\w.-]*)");
            Pattern ConnectPattern = Pattern.compile("CONNECT \\s*([\\w.-]*):([0-9]*).*");
            Matcher HostMatcher = HostPattern.matcher(line);
            Matcher ConnectMatcher = ConnectPattern.matcher(line);
            if (ConnectMatcher.find()) {  // connect to HTTPS
                this.Host = ConnectMatcher.group(1);
                this.port = Integer.parseInt(ConnectMatcher.group(2));
                break;
            }
            if (HostMatcher.find()) { // connect to HTTP
                this.Host = HostMatcher.group(1);
                this.port = 80;
                break;
            }
        }
    }

    int getPort() {
        return this.port;
    }

    String getHost() {
        return this.Host;
    }
}
