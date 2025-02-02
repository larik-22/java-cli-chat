package client.transfer;

import client.connection.ServerConnection;

import java.io.*;

/**
 * SenderClient class.
 * Handles the file transfer process for the sender side.
 */
public class SenderClient extends FileTransferClient {
    public SenderClient(ServerConnection serverConnection, String sessionUuid, File file) {
        super(serverConnection, sessionUuid, file);
    }

    @Override
    protected void handleFileTransfer() {
        try (InputStream fis = new BufferedInputStream(new FileInputStream(file));
             OutputStream os = new BufferedOutputStream(fileTransferConnection.getSocket().getOutputStream())) {
            fis.transferTo(os);
            os.flush();
        } catch (Exception e) {
        } finally {
            try {
                if (fileTransferConnection != null && fileTransferConnection.getSocket() != null) {
                    fileTransferConnection.getSocket().close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
