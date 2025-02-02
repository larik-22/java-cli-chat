package server.messages.handlers;

import server.connection.ClientRegistry;
import server.pingpong.PingPongManager;
import server.connection.ClientConnection;
import server.errors.ClientException;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.Enter;
import shared.messages.model.server.EnterResp;
import shared.messages.model.server.Joined;

public class EnterHandler implements MessageHandler<Enter> {
    private final ClientConnection clientConnection;
    private final PingPongManager pingPongManager;

    public EnterHandler(ClientConnection clientConnection, PingPongManager pingPongManager) {
        this.clientConnection = clientConnection;
        this.pingPongManager = pingPongManager;
    }

    @Override
    public void handle(Enter message) {
        try {
            ClientRegistry.getInstance().logInClient(message.username(), clientConnection);
            EnterResp enterResp = new EnterResp("OK", null);
            clientConnection.sendMessage(enterResp);
            pingPongManager.start();

            ClientRegistry.getInstance().getLoggedInClients().values().stream()
                    .filter(c -> !c.equals(clientConnection))
                    .forEach(c -> c.sendMessage(new Joined(message.username())));

        } catch (ClientException e) {
            clientConnection.sendMessage(new EnterResp("ERROR", e.getErrorCode()));
        }
    }
}
