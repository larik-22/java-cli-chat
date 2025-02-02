package server.transfer;

import server.logger.LogEntry;
import server.logger.ServerLogger;
import shared.constants.ConsoleColors;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The file transfer server class is responsible for starting
 * the file transfer server and handling incoming file transfer connections.
 * For each incoming connection, a new {@link FileTransferHandler} is started in a separate thread.
 */
public class FileTransferServer implements Runnable{
    private final int port;

    public FileTransferServer(int port) {
        this.port = port;
    }

    /**
     * Starts the file transfer server and listens for incoming connections.
     */
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (serverSocket.isBound()) {
                Socket clientSocket = serverSocket.accept();

                ServerLogger.log(new LogEntry.Builder()
                        .logType(ServerLogger.LogType.MESSAGE)
                        .clientAddress(clientSocket.getInetAddress().getHostAddress())
                        .clientPort(clientSocket.getPort())
                        .direction("->")
                        .message("Transfer user connected")
                        .color(ConsoleColors.GREEN.getColor())
                        .build());


                new Thread(new FileTransferHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting file transfer server: " + e.getMessage());
        }
    }
}
