package client.connection;

import shared.connection.Connection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * A base class for the server connection.
 * It is responsible for creating a connection to the server and handling the connection state.
 * Also responsible for storing user files and session ids.
 */
public class ServerConnection implements Connection {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected;
    private final String ip;
    private final int port;
    private final Map<String, String> userFiles = new ConcurrentHashMap<>();
    private final Map<String, List<String>> sessionIds = new ConcurrentHashMap<>();

    public ServerConnection(String ip, int port) {
        connected = false;

        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    public PrintWriter getOutputStream() {
        return out;
    }

    @Override
    public Socket getSocket(){
        return clientSocket;
    }

    public void connectToServer() {
        ScheduledExecutorService executor = newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                clientSocket = new Socket(ip, port);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                synchronized (this) {
                    connected = true;
                    notifyAll();
                }

                executor.shutdown();
            } catch (Exception e) {
                System.err.println("Failed to connect to server, retrying in 5 seconds...");
            }
        }, 0, 5, TimeUnit.SECONDS);

        synchronized (this) {
            while (!connected) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Connection attempt interrupted");
                }
            }
        }
    }

    public void sendMessage(String message) {
        getOutputStream().println(message);
        getOutputStream().flush();
    }

    @Override
    public void closeConnection() {
        connected = false;
        if (clientSocket != null) {
            try {
                clientSocket.close();
                connected = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (out != null) {
            out.close();
        }

        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.exit(0);
    }

    /**
     * Adds a file to the userFiles map.
     * @param filename The name of the file.
     * @param filepath The path to the file.
     */
    public void addFile(String filename, String filepath) {
        userFiles.put(filename, filepath);
    }

    /**
     * Returns the path to a file.
     * @param filename The name of the file.
     * @return The path to the file.
     */
    public String getFilePath(String filename) {
        return userFiles.get(filename);
    }

    /**
     * Adds a session id to the list of session ids for a sender.
     * @param sender The sender to add the session id for.
     * @param sessionId The session id to add.
     */
    public void addSessionId(String sender, String sessionId) {
        sessionIds.computeIfAbsent(sender, k -> new CopyOnWriteArrayList<>()).add(sessionId);
    }

    /**
     * Returns a list of session ids for a sender.
     * @param sender The sender to get the session ids for.
     * @return A list of session ids for the sender.
     */
    public List<String> getSessionIds(String sender) {
        return sessionIds.get(sender);
    }

    /**
     * Removes a session id from the list of session ids for a sender.
     * @param sessionId The session id to remove.
     */
    public void removeSessionId(String sessionId) {
        for (Map.Entry<String, List<String>> entry : sessionIds.entrySet()) {
            entry.getValue().remove(sessionId);
            if (entry.getValue().isEmpty()) {
                sessionIds.remove(entry.getKey());
            }
        }
    }

}
