package client.messages.handlers;

import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.Private;
import shared.utils.PrintingUtils;

public class PrivateHandler implements MessageHandler<Private> {
    @Override
    public void handle(Private message) {
        PrintingUtils.printMessage(message.from() + ": " + message.message(), ConsoleColors.GREEN);
    }
}
