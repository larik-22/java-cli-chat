package server.messages.handlers;

import server.connection.ClientConnection;
import server.connection.ClientRegistry;
import server.errors.ErrorCode;
import server.model.TransferSession;
import server.transfer.FileTransferManager;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.TransferReject;
import shared.messages.model.server.TransferRejectResp;
import shared.messages.model.server.TransferRejected;
import shared.messages.model.server.TransferResp;

public class TransferRejectHandler implements MessageHandler<TransferReject> {
    private final ClientConnection clientConnection;

    public TransferRejectHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void handle(TransferReject message) {
        ClientRegistry clientRegistry = ClientRegistry.getInstance();
        FileTransferManager fileTransferManager = FileTransferManager.getInstance();

        if (clientConnection.getUsername() == null) {
            clientConnection.sendMessage(new TransferResp("ERROR", ErrorCode.NOT_LOGGED_IN.getCode()));
            return;
        }

        // check if there is pending transfer session with this user
        TransferSession pendingSession = fileTransferManager.getPendingSession(message.id());

        // if there is no pending session, send error
        if (pendingSession == null) {
            clientConnection.sendMessage(new TransferRejectResp("ERROR", ErrorCode.COMMAND_NOT_EXPECTED.getCode()));
            return;
        }

        if (!pendingSession.getReceiver().equals(clientConnection.getUsername())) {
            clientConnection.sendMessage(new TransferRejectResp("ERROR", ErrorCode.COMMAND_NOT_EXPECTED.getCode()));
            return;
        }

        // remove the pending session
        fileTransferManager.removePendingSession(pendingSession.getSender(), clientConnection.getUsername());

        // send back the reject response and inform the sender
        clientConnection.sendMessage(new TransferRejectResp("OK", null));

        // inform the sender that the transfer has been rejected
        ClientConnection senderConnection = clientRegistry.getClientByUsername(pendingSession.getSender());
        senderConnection.sendMessage(new TransferRejected(clientConnection.getUsername()));
    }
}
