package server.messages.handlers;

import server.connection.ClientRegistry;
import server.connection.ClientConnection;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.PrivateReq;
import shared.messages.model.server.Private;
import shared.messages.model.server.PrivateResp;

public class PrivateReqHandler implements MessageHandler<PrivateReq> {
    private final ClientConnection clientConnection;

    public PrivateReqHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void handle(PrivateReq message) {
        if(clientConnection.getUsername() == null){
            clientConnection.sendMessage(new PrivateResp("ERROR", 6000));
            return;
        }

        String recipient = message.to();
        ClientConnection recipientConnection = ClientRegistry.getInstance().getClientByUsername(recipient);

        // check if recipient exists
        if(recipientConnection == null){
            clientConnection.sendMessage(new PrivateResp("ERROR", 9001));
            return;
        }

        // check if user tries to send a message to themselves
        if(recipientConnection.equals(clientConnection)){
            clientConnection.sendMessage(new PrivateResp("ERROR", 9002));
            return;
        }

        recipientConnection.sendMessage(new Private(clientConnection.getUsername(), message.message()));
        clientConnection.sendMessage(new PrivateResp("OK", null));
    }
}
