package server.messages.handlers;

import server.connection.ClientRegistry;
import server.connection.ClientConnection;
import server.errors.ErrorCode;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.ClientsReq;
import shared.messages.model.server.Clients;
import shared.messages.model.server.ClientsResp;

public class ClientsReqHandler implements MessageHandler<ClientsReq> {
    private final ClientConnection clientConnection;

    public ClientsReqHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void handle(ClientsReq message) {
        if(clientConnection.getUsername() == null) {
            clientConnection.sendMessage(new ClientsResp("error", ErrorCode.NOT_LOGGED_IN.getCode()));
            return;
        }

        // send all clients except the client that requested the list
        clientConnection.sendMessage(new Clients(
                ClientRegistry.getInstance().getLoggedInClients().values().stream()
                        .filter(connection -> connection != clientConnection)
                        .map(ClientConnection::getUsername)
                        .toList()
        ));
    }
}
