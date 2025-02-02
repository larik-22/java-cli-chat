package server.transfer;

import server.model.TransferSession;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * The file transfer handler class is responsible for handling file transfer connections.
 * Represents a connection handler for file transfer connections. B
 */
public class FileTransferHandler implements Runnable {
    private final Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public FileTransferHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * Starts the file transfer handler and handles the file transfer connection.
     * First user is identified by UUID, then the transfer type is determined.
     * Once both sender and receiver are identified, the file transfer is initiated.
     */
    @Override
    public void run() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            String uuid = readUuid();
            char transferType = uuid.charAt(36);
            uuid = uuid.substring(0, 36);

            TransferSession session = retrieveTransferSession(uuid);
            assignStreamsToSession(session, transferType);

            if (session.getSenderInputStream() != null && session.getReceiverOutputStream() != null) {
                transferFile(session);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the UUID from the input stream.
     * @return The UUID, including transfer type, read from the input stream.
     * @throws IOException If an I/O error occurs.
     */
    private String readUuid() throws IOException {
        byte[] buffer = new byte[37];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead != 37) {
            throw new IOException("Invalid UUID received");
        }
        return new String(buffer);
    }

    /**
     * Assigns the input and output streams to the transfer session based on the transfer type.
     * @param session The transfer session.
     * @param transferType The transfer type.
     * @throws IOException If an I/O error occurs.
     */
    private void assignStreamsToSession(TransferSession session, char transferType) throws IOException {
        if (transferType == 's') {
            session.setSenderInputStream(inputStream);
        } else if (transferType == 'r') {
            session.setReceiverOutputStream(outputStream);
        } else {
            throw new IOException("Invalid transfer type: " + transferType);
        }
    }

    /**
     * Transfers the file bytes from the sender to the receiver.
     * @param session The transfer session.
     */
    private void transferFile(TransferSession session) {
        try (InputStream senderStream = session.getSenderInputStream();
             OutputStream receiverStream = new BufferedOutputStream(session.getReceiverOutputStream())) {
            senderStream.transferTo(receiverStream);
            receiverStream.flush();
        } catch (Exception e) {
            //
        } finally {
            closeStreams(session);
        }
    }

    /**
     * Closes the input and output streams.
     * @param session The transfer session.
     */
    private void closeStreams(TransferSession session) {
        try {
            session.getSenderInputStream().close();
            session.getReceiverOutputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the transfer session based on the UUID.
     * @param uuid The UUID.
     * @return The transfer session.
     * @throws IOException If an I/O error occurs.
     */
    private TransferSession retrieveTransferSession(String uuid) throws IOException {
        TransferSession session = FileTransferManager.getInstance().getActiveSession(uuid);
        if (session == null) {
            throw new IOException("No active transfer session found for UUID: " + uuid);
        }
        return session;
    }
}
