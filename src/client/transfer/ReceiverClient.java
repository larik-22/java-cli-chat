package client.transfer;

import client.connection.ServerConnection;
import shared.messages.model.client.TransferChecksum;
import shared.utils.FileTransferUtils;
import shared.utils.JsonUtils;

import java.io.*;

/**
 * ReceiverClient class.
 * Handles the file transfer process for the receiver side.
 */
public class ReceiverClient extends FileTransferClient {
    public ReceiverClient(ServerConnection serverConnection, String sessionUuid, String filename) {
        super(serverConnection, sessionUuid, new File("resources/received/" + filename));
        serverConnection.addFile(filename, file.getPath());
    }

    @Override
    protected void handleFileTransfer() {
        try (InputStream is = new BufferedInputStream(fileTransferConnection.getSocket().getInputStream());
             OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
            is.transferTo(fos);
            fos.flush();
            fos.close();

            String checksum = FileTransferUtils.calculateChecksum(file);
            serverConnection.sendMessage(JsonUtils.classToMessage(new TransferChecksum(getOriginalSessionUuid(), checksum)));
        } catch (Exception e) {
            e.printStackTrace();
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
