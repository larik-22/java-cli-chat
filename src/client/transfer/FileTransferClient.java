package client.transfer;

import client.connection.FileTransferConnection;
import client.connection.ServerConnection;
import client.constants.ClientConfig;

import java.io.File;
import java.io.IOException;

/**
 * A base class for file transfer clients.
 * Responsible for establishing a connection with the server and handling the file transfer.
 */
public abstract class FileTransferClient implements Runnable {
    protected ServerConnection serverConnection;
    protected FileTransferConnection fileTransferConnection;
    protected String sessionUuid;
    protected File file;

    public FileTransferClient(ServerConnection serverConnection, String sessionUuid, File file) {
        this.sessionUuid = sessionUuid;
        this.file = file;
        this.serverConnection = serverConnection;
    }

    @Override
    public void run() {
        try {
            fileTransferConnection = new FileTransferConnection(serverConnection, ClientConfig.SERVER_IP, ClientConfig.FILE_TRANSFER_PORT);
            fileTransferConnection.getSocket().getOutputStream().write(sessionUuid.getBytes());

            handleFileTransfer();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileTransferConnection != null) {
                try {
                    fileTransferConnection.getSocket().close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public String getOriginalSessionUuid() {
        return sessionUuid.substring(0, sessionUuid.length() - 1);
    }

    protected abstract void handleFileTransfer();
}
