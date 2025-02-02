package client.connection;

import shared.connection.Connection;

import java.io.IOException;
import java.net.Socket;

/**
 * This class is used to establish a connection to the file transfer server.
 */
public class FileTransferConnection implements Connection {
    private final ServerConnection serverConnection;
    private Socket fileTransferSocket;

    public FileTransferConnection(ServerConnection serverConnection, String ip, int port) {
        this.serverConnection = serverConnection;
        try {
            fileTransferSocket = new Socket(ip, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isConnected() {
        return fileTransferSocket.isConnected();
    }

    @Override
    public Socket getSocket() {
        return fileTransferSocket;
    }

    public ServerConnection getUserConnection() {
        return serverConnection;
    }

    @Override
    public void closeConnection() {
        try {
            fileTransferSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
