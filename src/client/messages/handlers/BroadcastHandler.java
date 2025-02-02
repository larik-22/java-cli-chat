package client.messages.handlers;

import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.Broadcast;
import shared.utils.PrintingUtils;

public class BroadcastHandler implements MessageHandler<Broadcast> {
    @Override
    public void handle(Broadcast message) {
        PrintingUtils.printMessage(message.username() + ": " + message.message(), ConsoleColors.YELLOW);
    }
}
