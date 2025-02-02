package server.messages.handlers;

import server.connection.ClientRegistry;
import server.connection.ClientConnection;
import server.errors.ErrorCode;
import server.model.GameSession;
import server.rps.GameManager;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.RPSStartReq;
import shared.messages.model.server.RPSStart;
import shared.messages.model.server.RPSStartResp;

import java.util.List;

public class RPSStartReqHandler implements MessageHandler<RPSStartReq> {
    private final ClientConnection clientConnection;

    public RPSStartReqHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void handle(RPSStartReq message) {
        // not logged in
        if(clientConnection.getUsername() == null){
            clientConnection.sendMessage(new RPSStartResp("ERROR", ErrorCode.NOT_LOGGED_IN.getCode()));
            return;
        }

        // recipient doesn't exist
        String recipient = message.username();
        ClientConnection recipientConnection = ClientRegistry.getInstance().getClientByUsername(recipient);

        // check if recipient exists
        if(recipientConnection == null){
            clientConnection.sendMessage(new RPSStartResp("ERROR", ErrorCode.RECEIVER_NOT_FOUND.getCode()));
            return;
        }

        // check if user tries to send a message to themselves
        if(recipientConnection.equals(clientConnection)){
            clientConnection.sendMessage(new RPSStartResp("ERROR", ErrorCode.INVALID_RECEIVER.getCode()));
            return;
        }

        GameManager gameManager = GameManager.getInstance();

        // check if two users are already playing
        if(gameManager.isGameActive()){
            List<String> players = List.of(gameManager.getActiveGame().getPlayer1(), gameManager.getActiveGame().getPlayer2());
            clientConnection.sendMessage(new RPSStartResp("ERROR", ErrorCode.TWO_USERS_ALREADY_PLAYING.getCode(), players));
            return;
        }

        // start the game
        GameSession gameSession = new GameSession(clientConnection.getUsername(), recipient);
        gameManager.setActiveGame(gameSession);

        // notify both players
        clientConnection.sendMessage(new RPSStartResp("OK"));
        recipientConnection.sendMessage(new RPSStart(clientConnection.getUsername()));

    }
}
