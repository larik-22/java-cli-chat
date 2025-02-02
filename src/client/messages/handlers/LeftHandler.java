package client.messages.handlers;

import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.Left;
import shared.utils.PrintingUtils;

public class LeftHandler implements MessageHandler<Left> {
    @Override
    public void handle(Left message) {
        PrintingUtils.printMessage("Left: " + message.username(), ConsoleColors.RED);
    }
}
