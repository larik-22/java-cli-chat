package client.messages.handlers;

import shared.messages.handling.MessageHandler;
import shared.messages.model.server.Hangup;

public class HangupHandler implements MessageHandler<Hangup> {
    @Override
    public void handle(Hangup message) {
        System.out.println("Server has hung up. Exiting...");
        System.exit(0);
    }
}
