package server.model;

import server.connection.ClientConnection;

/**
 * This class represents a pair of connections that are used for file transfer.
 * It contains the sender and receiver connections and the session that is used for the transfer.
 */
public class TransferConnectionPair {
    private final ClientConnection sender;
    private final ClientConnection receiver;
    private final TransferSession session;

    public TransferConnectionPair(ClientConnection first, ClientConnection second, TransferSession session) {
        this.sender = first;
        this.receiver = second;
        this.session = session;
    }

    public ClientConnection getSender() {
        return sender;
    }

    public ClientConnection getReceiver() {
        return receiver;
    }

    public TransferSession getSession() {
        return session;
    }
}
