package server.messages.handlers;

import server.connection.ClientConnection;
import server.connection.ClientRegistry;
import server.errors.ErrorCode;
import server.model.TransferSession;
import server.transfer.FileTransferManager;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.TransferAccept;
import shared.messages.model.server.TransferAcceptResp;
import shared.messages.model.server.TransferAccepted;
import shared.messages.model.server.TransferResp;

public class TransferAcceptHandler implements MessageHandler<TransferAccept> {
    private final ClientConnection clientConnection;

    public TransferAcceptHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void handle(TransferAccept message) {
        ClientRegistry clientRegistry = ClientRegistry.getInstance();
        FileTransferManager fileTransferManager = FileTransferManager.getInstance();

        if (clientConnection.getUsername() == null) {
            clientConnection.sendMessage(new TransferResp("ERROR", ErrorCode.NOT_LOGGED_IN.getCode()));
            return;
        }

        // check if there is pending transfer session by this id
        TransferSession session = fileTransferManager.getPendingSession(message.id());

        // if there is no pending session, send error
        if (session == null) {
            clientConnection.sendMessage(new TransferAcceptResp("ERROR", ErrorCode.COMMAND_NOT_EXPECTED.getCode()));
            return;
        }

        if (!session.getReceiver().equals(clientConnection.getUsername())) {
            clientConnection.sendMessage(new TransferAcceptResp("ERROR", ErrorCode.COMMAND_NOT_EXPECTED.getCode()));
            return;
        }

        // get the sender connection
        ClientConnection senderConnection = clientRegistry.getClientByUsername(session.getSender());

        // send accept_resp to the receiver
        clientConnection.sendMessage(new TransferAcceptResp("OK", null));

        // stop the timeout for the transfer request and create a new active session
        fileTransferManager.removePendingSession(senderConnection.getUsername(), clientConnection.getUsername());
        fileTransferManager.createActiveSession(session);

        // send accepted to both sender and receiver with the session uuid (append 's' or 'r' in the end)
        clientConnection.sendMessage(new TransferAccepted(session.getSender(), session.getFilename(), session.getId() + "r"));
        senderConnection.sendMessage(new TransferAccepted(clientConnection.getUsername(), session.getFilename(), session.getId() + "s"));
    }
}
