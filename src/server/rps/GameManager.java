package server.rps;

import server.constants.ServerConfig;
import server.model.GameSession;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The GameManager class is responsible for managing the rps game sessions.
 */
public class GameManager {
    private static final GameManager instance = new GameManager();
    private final AtomicReference<GameSession> activeGame = new AtomicReference<>(null);

    private GameManager() {}

    public static GameManager getInstance() {
        return instance;
    }

    /**
     * Checks if a game is active.
     *
     * @return true if a game is active, false otherwise
     */
    public boolean isGameActive() {
        return activeGame.get() != null;
    }

    /**
     * Gets the active game.
     */
    public GameSession getActiveGame() {
        return activeGame.get();
    }

    /**
     * Checks if a user is in a game.
     *
     * @param username - the username of the user
     * @return true if the user is in a game, false otherwise
     */
    public boolean isUserInGame(String username) {
        GameSession session = activeGame.get();
        return session != null && session.involves(username);
    }

    /**
     * Starts a game between two users.
     *
     * @param game - the game to start
     */
    public synchronized void setActiveGame(GameSession game) {
        activeGame.set(game);
        activeGame.get().getTimeoutManager().startGame(ServerConfig.RPS_CHOICE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * Ends the game.
     */
    public synchronized void endGame() {
        if(activeGame.get() != null){
            activeGame.get().getTimeoutManager().stopGame();
            activeGame.set(null);
        }
    }
}
