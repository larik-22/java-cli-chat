package client.messages.handlers;

import client.connection.ServerConnection;
import shared.messages.handling.MessageHandler;
import shared.constants.ProtocolCommands;
import shared.messages.model.server.Ping;

public class PingHandler implements MessageHandler<Ping> {
    private final ServerConnection serverConnection;

    public PingHandler(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void handle(Ping message) {
        serverConnection.sendMessage(ProtocolCommands.PONG.toString());
    }
}
