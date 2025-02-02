package shared.connection;

import java.net.Socket;

/**
 * Connection interface is used to define the methods that a connection should have.
 * This interface is implemented
 * by the {@link server.connection.ClientConnection} and {@link client.connection.ServerConnection} classes.
 */
public interface Connection {
    boolean isConnected();
    Socket getSocket();
    void closeConnection();
}