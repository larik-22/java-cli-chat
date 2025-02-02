package server.connection;

import server.logger.LogEntry;
import server.logger.ServerLogger;
import shared.connection.Connection;
import shared.utils.JsonUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Represents a connection to a client. This class is responsible for handling the connection to a client.
 * Implements {@link Connection} interface.
 */
public class ClientConnection implements Connection {
    private final Socket clientSocket;
    private String username;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Constructor for the ClientConnection class. It initializes the input and output streams for the client connection.
     * @param clientSocket the client socket
     */
    public ClientConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (Exception e) {
            System.err.println("Failed to get input/output streams: " + e.getMessage());
        }

        ClientRegistry.getInstance().addClient(this);
    }

    @Override
    public boolean isConnected() {
        return !clientSocket.isClosed();
    }
    
    public PrintWriter getOutputStream() {
        return out;
    }

    @Override
    public void closeConnection() {
        try {
            clientSocket.close();
            in.close();
            out.close();
        } catch (Exception e) {
            System.err.println("Failed to close client connection: " + e.getMessage());
        } finally {
            ClientRegistry.getInstance().removeClient(this);
            ServerLogger.logClientCount();
        }
    }

    @Override
    public Socket getSocket(){
        return clientSocket;
    }

    /**
     * Get the client's username
     * @return the client's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the client's username
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Send a message to the client
     * @param message the message to send
     */
    public void sendMessage(String message) {
        out.println(message);
    }

    /**
     * Sends a converted object message to the client and log it
     * @param message the object message to send
     */
    public void sendMessage(Object message) {
        out.println(JsonUtils.classToMessage(message));

        // Log the message
        ServerLogger.log(new LogEntry.Builder()
                .logType(ServerLogger.LogType.MESSAGE)
                .clientAddress(getClientAddress())
                .clientPort(getClientPort())
                .username(username)
                .direction("<--")
                .message(JsonUtils.classToMessage(message))
                .build());
    }

    /**
     * Get the client's address. Used for logging purposes.
     * @return the client's address
     */
    public String getClientAddress() {
        return clientSocket.getInetAddress().getHostAddress();
    }

    /**
     * Get the client's port. Used for logging purposes.
     * @return the client's port
     */
    public int getClientPort() {
        return clientSocket.getPort();
    }
}
