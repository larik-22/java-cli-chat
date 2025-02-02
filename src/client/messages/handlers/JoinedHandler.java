package client.messages.handlers;

import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.Joined;
import shared.utils.PrintingUtils;

public class JoinedHandler implements MessageHandler<Joined> {
    @Override
    public void handle(Joined message) {
        PrintingUtils.printMessage(message.username() + " has joined the chat.", ConsoleColors.GREEN);
    }
}
