package server.rps;

import server.connection.ClientConnection;
import server.model.GameSession;
import server.timeout.RPSTimeoutHandler;
import server.timeout.TimeoutManager;

import java.util.concurrent.TimeUnit;

public class RPSGame {
    private final GameSession gameSession;
    private final ClientConnection player1Connection;
    private final ClientConnection player2Connection;
    private final TimeoutManager<GameSession> timeoutManager;

    public RPSGame(GameSession gameSession, ClientConnection player1Connection, ClientConnection player2Connection) {
        this.gameSession = gameSession;
        this.player1Connection = player1Connection;
        this.player2Connection = player2Connection;
        this.timeoutManager = new TimeoutManager<>(gameSession, new RPSTimeoutHandler(player1Connection, player2Connection));
    }

    public void startGame(long timeout, TimeUnit timeUnit) {
        timeoutManager.startTimeout(timeout, timeUnit);
    }

    public void stopGame() {
        timeoutManager.stop();
    }
}