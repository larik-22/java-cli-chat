package server.timeout;

import server.connection.ClientConnection;
import server.errors.ErrorCode;
import server.model.GameSession;
import server.rps.GameManager;
import shared.messages.model.server.RPSChoiceResp;

/**
 * The RPSTimeoutHandler class is responsible for handling the timeout of the Rock-Paper-Scissors game.
 * It sends an error message to both players and ends the game.
 * Used within {@link server.rps.GameManager}.
 */
public class RPSTimeoutHandler implements TimeoutHandler<GameSession> {
    private final ClientConnection player1Connection;
    private final ClientConnection player2Connection;

    public RPSTimeoutHandler(ClientConnection player1Connection, ClientConnection player2Connection) {
        this.player1Connection = player1Connection;
        this.player2Connection = player2Connection;
    }

    @Override
    public boolean isConditionFulfilled(GameSession session) {
        return session.isComplete();
    }

    @Override
    public void onTimeout(GameSession session) {
        RPSChoiceResp timeoutResponse = new RPSChoiceResp("ERROR", ErrorCode.RESPONSE_TIMEOUT.getCode());
        player1Connection.sendMessage(timeoutResponse);
        player2Connection.sendMessage(timeoutResponse);

        GameManager.getInstance().endGame();
    }
}
