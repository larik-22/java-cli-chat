package server.messages.handlers;

import server.connection.ClientRegistry;
import server.connection.ClientConnection;
import server.errors.ErrorCode;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.BroadcastReq;
import shared.messages.model.server.Broadcast;
import shared.messages.model.server.BroadcastResp;

public class BroadcastReqHandler implements MessageHandler<BroadcastReq> {
    private final ClientConnection clientConnection;

    public BroadcastReqHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void handle(BroadcastReq message) {
        if (clientConnection.getUsername() == null){
            BroadcastResp broadcastResp = new BroadcastResp("ERROR", ErrorCode.NOT_LOGGED_IN.getCode());
            clientConnection.sendMessage(broadcastResp);
            return;
        } else {
            BroadcastResp broadcastResp = new BroadcastResp("OK", null);
            clientConnection.sendMessage(broadcastResp);
        }

        for (ClientConnection connection : ClientRegistry.getInstance().getLoggedInClients().values()) {
            if (connection != clientConnection) {
                connection.sendMessage(new Broadcast(clientConnection.getUsername(), message.message()));
            }
        }
    }
}
