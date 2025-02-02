package client.messages.handlers;

import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.Ready;
import shared.utils.PrintingUtils;

public class ReadyHandler implements MessageHandler<Ready> {
    @Override
    public void handle(Ready message) {
        PrintingUtils.printMessage("Server is ready. Version: " + message.version(), ConsoleColors.GREEN);
    }
}
