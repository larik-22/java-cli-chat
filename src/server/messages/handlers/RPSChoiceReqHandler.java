package server.messages.handlers;

import server.connection.ClientConnection;
import server.connection.ClientRegistry;
import server.errors.ErrorCode;
import server.rps.GameManager;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.RPSChoiceReq;
import shared.messages.model.server.RPSChoiceResp;
import shared.messages.model.server.RPSEnd;

import java.util.List;

public class RPSChoiceReqHandler implements MessageHandler<RPSChoiceReq> {
    private final ClientConnection clientConnection;

    public RPSChoiceReqHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void handle(RPSChoiceReq message) {
        ClientRegistry clientRegistry = ClientRegistry.getInstance();
        GameManager gameManager = GameManager.getInstance();

        // check if user: already made a choice, is playing, is not playing, choice timeout, if logged in
        if(clientConnection.getUsername() == null){
            clientConnection.sendMessage(new RPSChoiceResp("ERROR", ErrorCode.NOT_LOGGED_IN.getCode()));
            return;
        }

        // no game started
        if(!gameManager.isGameActive()){
            clientConnection.sendMessage(new RPSChoiceResp("ERROR", ErrorCode.NO_GAME_ACTIVE.getCode()));
            return;
        }

        // not a participant (unexpected command)
        if(!gameManager.getActiveGame().involves(clientConnection.getUsername())){
            clientConnection.sendMessage(new RPSChoiceResp("ERROR", ErrorCode.COMMAND_NOT_EXPECTED.getCode()));
            return;
        }

        // invalid choice
        if(!List.of("rock", "paper", "scissors").contains(message.choice())){
            clientConnection.sendMessage(new RPSChoiceResp("ERROR", ErrorCode.INVALID_RPS_CHOICE.getCode()));
            return;
        }

        // already made a choice
        if(gameManager.getActiveGame().getPlayerChoice(clientConnection.getUsername()) != null){
            clientConnection.sendMessage(new RPSChoiceResp("ERROR", ErrorCode.ALREADY_MADE_A_CHOICE.getCode()));
            return;
        }

        // set the choice for the player
        gameManager.getActiveGame().recordChoice(clientConnection.getUsername(), message.choice());

        // check if both players have made a choice and send the result
        if(gameManager.getActiveGame().isComplete()){
            gameManager.getActiveGame().getTimeoutManager().stopGame();

            String winner = gameManager.getActiveGame().determineWinner();
            String opponentChoice = gameManager.getActiveGame().getPlayerChoice(gameManager.getActiveGame().getOpponent(clientConnection.getUsername()));
            ClientConnection opponentConnection =  clientRegistry.getClientByUsername(gameManager.getActiveGame().getOpponent(clientConnection.getUsername()));

            // send the result to both players
            clientConnection.sendMessage(new RPSEnd(winner, opponentChoice));
            opponentConnection.sendMessage(new RPSEnd(winner, message.choice()));

            // end the game
            gameManager.endGame();
        }
    }
}
