package server.messages.handlers;

import server.connection.ClientRegistry;
import server.connection.ClientConnection;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.Bye;
import shared.messages.model.server.ByeResp;
import shared.messages.model.server.Left;

public class ByeHandler implements MessageHandler<Bye> {
    private final ClientConnection clientConnection;

    public ByeHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void handle(Bye message) {
        // send bye_resp and close the connection
        clientConnection.sendMessage(new ByeResp("OK"));

        // send LEFT message to all other clients
        for(ClientConnection connection:ClientRegistry.getInstance().getLoggedInClients().values()){
            if(connection != clientConnection && clientConnection.getUsername() != null){
                connection.sendMessage(new Left(clientConnection.getUsername()));
            }
        }

        // remove the client from the registry
        clientConnection.closeConnection();
    }
}
