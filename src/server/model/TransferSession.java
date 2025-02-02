package server.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * The TransferSession class represents a session between two clients that are transferring a file.
 * It contains information about the sender, receiver, filename, checksum, and the session id.
 */
public class TransferSession {
    private final String sender;
    private final String receiver;
    private final String filename;
    private final String checksum;
    private final String sessionId;
    private InputStream senderInputStream;
    private OutputStream receiverOutputStream;

    public TransferSession(String sender, String receiver, String filename, String checksum) {
        this.sessionId = UUID.randomUUID().toString();
        this.sender = sender;
        this.receiver = receiver;
        this.filename = filename;
        this.checksum = checksum;
    }

    public InputStream getSenderInputStream() {
        return senderInputStream;
    }

    public OutputStream getReceiverOutputStream() {
        return receiverOutputStream;
    }

    public void setSenderInputStream(InputStream inputStream) {
        this.senderInputStream = inputStream;
    }

    public void setReceiverOutputStream(OutputStream outputStream) {
        this.receiverOutputStream = outputStream;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getFilename() {
        return filename;
    }

    public String getId() {
        return sessionId;
    }

}

