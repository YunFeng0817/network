package Lab1;

import java.net.Socket;

/**
 * this interface is used to specific the proxy forward function between two socket: client and server
 */
public interface ProxyForward {
    void ProxyForward(Socket client, Socket server, long MaxWaitTime);
}
