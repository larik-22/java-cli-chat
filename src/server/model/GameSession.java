package server.model;

import server.rps.RPSGame;
import server.connection.ClientRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The GameSession class represents a game session between two players. It records the choices of the players and
 * determines the winner of the game session.
 * Used by the {@link server.rps.RPSGame} class.
 */
public class GameSession {
    private final String player1;
    private final String player2;
    private final Map<String, String> playerChoice = new ConcurrentHashMap<>();
    private final RPSGame timeoutManager;

    public GameSession(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.timeoutManager = new RPSGame(this, ClientRegistry.getInstance().getClientByUsername(player1),
                ClientRegistry.getInstance().getClientByUsername(player2));
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public RPSGame getTimeoutManager() {
        return timeoutManager;
    }

    /**
     * Check if the game session involves a player
     *
     * @param player - the player to check
     * @return true if the player is involved in the game session, false otherwise
     */
    public boolean involves(String player) {
        return player1.equals(player) || player2.equals(player);
    }

    /**
     * Get the opponent of a player in the game session
     *
     * @param player - the player to get the opponent for
     * @return the opponent of the player
     */
    public String getOpponent(String player) {
        if (player1.equals(player)) {
            return player2;
        } else if (player2.equals(player)) {
            return player1;
        } else {
            return null;
        }
    }

    /**
     * Record the choice of a player in the game session
     *
     * @param player - the player to record the choice for
     * @param choice - the choice to record
     */
    public void recordChoice(String player, String choice) {
        playerChoice.put(player, choice);
    }

    /**
     * Get the choice of a player in the game session
     *
     * @param player - the player to get the choice for
     * @return the choice of the player
     */
    public String getPlayerChoice(String player) {
        try {
            return playerChoice.get(player);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if the game session is complete
     *
     * @return true if the game session is complete, false otherwise
     */
    public boolean isComplete() {
        return playerChoice.size() == 2;
    }

    /**
     * Returns a string representing the winner of the game session
     * @return the winner of the game session
     */
    public String determineWinner() {
        String player1choice = playerChoice.get(player1);
        String player2choice = playerChoice.get(player2);

        if (player1choice.equals(player2choice)) {
            return null;
        }

        Map<String, String> winningMoves = Map.of(
                "rock", "scissors",
                "scissors", "paper",
                "paper", "rock"
        );

        if (winningMoves.get(player1choice).equals(player2choice)) {
            return player1;
        } else {
            return player2;
        }
    }
}
