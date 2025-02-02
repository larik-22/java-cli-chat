package server.pingpong;

import server.connection.ClientConnection;
import server.constants.ServerConfig;
import shared.messages.model.server.Hangup;
import shared.messages.model.server.Ping;
import shared.messages.model.server.PongError;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class that manages the ping pong messages between the server and the client.
 * Uses {@link ScheduledExecutorService} to send PING messages to the client at regular intervals.
 * If a PONG message is not received in time, the connection is closed.
 */
public class PingPongManager {
    private final ClientConnection clientConnection;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean expectingPong = new AtomicBoolean(false);

    public PingPongManager(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    /**
     * Starts the ping pong manager thread
     */
    public void start() {
        scheduler.scheduleAtFixedRate(this::sendPing, ServerConfig.PING_INTERVAL, ServerConfig.PING_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Sends a PING message to the client
     */
    private synchronized void sendPing() {
        if (!clientConnection.isConnected()) {
            stop();
            return;
        }

        if (expectingPong.get()) {
            sendHangupAndClose(7000);
            return;
        }

        try {
            if (!scheduler.isShutdown()) {
                clientConnection.sendMessage(new Ping());
                expectingPong.set(true);
                scheduler.schedule(this::checkPongTimeout, ServerConfig.PONG_TIMEOUT, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            System.err.println("Failed to send PING: " + e.getMessage());
            stop();
        }

    }

    /**
     * Checks if a PONG message was received in time.
     */
    private synchronized void checkPongTimeout() {
        if (expectingPong.get() && clientConnection.isConnected()) {
            sendHangupAndClose(7000);
        }
    }

    /**
     * Handles a PONG message.
     * Sends a PONG_ERROR if not expecting a PONG
     */
    public synchronized void handlePong() {
        if (!clientConnection.isConnected()) {
            stop();
            return;
        }

        if (!expectingPong.get()) {
            clientConnection.sendMessage(new PongError(8000));
        } else {
            expectingPong.set(false);
        }
    }

    /**
     * Sends a HANGUP message and closes the connection
     * @param reasonCode the reason code to send
     */
    private void sendHangupAndClose(int reasonCode) {
        try {
            clientConnection.sendMessage(new Hangup(reasonCode));
        } finally {
            clientConnection.closeConnection();
            stop();
        }
    }

    /**
     * Stops the ping pong manager thread
     */
    public void stop() {
        scheduler.shutdownNow();
        scheduler.close();
    }
}
