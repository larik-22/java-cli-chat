package server.messages.handlers;

import server.connection.ClientConnection;
import server.connection.ClientRegistry;
import server.errors.ErrorCode;
import server.model.TransferSession;
import server.timeout.TimeoutManager;
import server.timeout.TransferReqTimeoutHandler;
import server.transfer.FileTransferManager;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.TransferReq;
import shared.messages.model.server.TransferResp;

public class TransferReqHandler implements MessageHandler<TransferReq> {
    private final ClientConnection clientConnection;
    private TimeoutManager<ClientConnection> timeoutManager;
    private TransferReqTimeoutHandler timeoutHandler;

    public TransferReqHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void handle(TransferReq message) {
        // check if user is logged in
        if (clientConnection.getUsername() == null) {
            clientConnection.sendMessage(new TransferResp("ERROR", ErrorCode.NOT_LOGGED_IN.getCode()));
            return;
        }

        // check if receiver exists
        ClientConnection receiverConnection = ClientRegistry.getInstance().getClientByUsername(message.username());
        if (receiverConnection == null) {
            clientConnection.sendMessage(new TransferResp("ERROR", ErrorCode.RECEIVER_NOT_FOUND.getCode()));
            return;
        }

        // check if receiver is not the sender
        if (clientConnection.getUsername().equals(message.username())) {
            clientConnection.sendMessage(new TransferResp("ERROR", ErrorCode.INVALID_RECEIVER.getCode()));
            return;
        }

        // send transfer request to receiver
        TransferSession session = new TransferSession(clientConnection.getUsername(), message.username(), message.filename(), message.checksum());
        TransferReq transferReq = new TransferReq(clientConnection.getUsername(), message.filename(), message.filesize(), message.checksum(), session.getId());
        receiverConnection.sendMessage(transferReq);

        // add pending session and start timeout
        FileTransferManager.getInstance().addPendingSession(session);

        // inform sender that the transfer request has been sent
        clientConnection.sendMessage(new TransferResp("OK", null));
    }

}
