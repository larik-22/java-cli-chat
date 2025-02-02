package server.messages.handlers;

import server.connection.ClientConnection;
import server.connection.ClientRegistry;
import server.errors.ErrorCode;
import server.model.TransferSession;
import server.transfer.FileTransferManager;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.TransferChecksum;
import shared.messages.model.server.TransferFailed;
import shared.messages.model.server.TransferSuccess;

public class TransferChecksumHandler implements MessageHandler<TransferChecksum> {
    private final ClientConnection clientConnection;

    public TransferChecksumHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void handle(TransferChecksum message) {
        // check if checksum is correct
        FileTransferManager manager = FileTransferManager.getInstance();
        ClientRegistry registry = ClientRegistry.getInstance();

        // get the session
        TransferSession session = manager.getActiveSession(message.sessionUuid());
        if (session == null) return;

        ClientConnection sender = registry.getClientByUsername(session.getSender());

        // check if checksum is correct
        if (session.getChecksum().equals(message.checksum())) {
            // send completed message to both
            sender.sendMessage(new TransferSuccess(session.getId(), session.getFilename()));
            clientConnection.sendMessage(new TransferSuccess(session.getId(), session.getFilename()));
        } else {
            // send error message
            sender.sendMessage(new TransferFailed(session.getId(), session.getFilename(), ErrorCode.CHECKSUMS_NOT_EQUAL.getCode()));
            clientConnection.sendMessage(new TransferFailed(session.getId(), session.getFilename(), ErrorCode.CHECKSUMS_NOT_EQUAL.getCode()));
        }

        // remove session
        manager.removeActiveSession(message.sessionUuid());
    }
}
