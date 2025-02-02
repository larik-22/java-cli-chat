package server.messages.handlers;

import server.pingpong.PingPongManager;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.Pong;

public class PongHandler implements MessageHandler<Pong> {
    private final PingPongManager pingPongManager;

    public PongHandler(PingPongManager pingPongManager) {
        this.pingPongManager = pingPongManager;
    }

    @Override
    public void handle(Pong message) {
        pingPongManager.handlePong();
    }
}
