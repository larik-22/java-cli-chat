package client.messages.handlers;

import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.PongError;

public class PongErrorHandler implements MessageHandler<PongError> {
    @Override
    public void handle(PongError message) {
        MessageErrorHandler.getInstance().handleError(message.code());
    }
}
