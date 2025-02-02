package client.messages.handlers;

import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.RpsError;

public class RpsErrorHandler implements MessageHandler<RpsError> {
    @Override
    public void handle(RpsError message) {
        MessageErrorHandler.getInstance().handleError(message.code());
    }
}
