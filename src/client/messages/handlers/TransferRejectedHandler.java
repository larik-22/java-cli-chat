package client.messages.handlers;

import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.TransferRejected;
import shared.utils.PrintingUtils;

public class TransferRejectedHandler implements MessageHandler<TransferRejected> {
    @Override
    public void handle(TransferRejected message) {
        PrintingUtils.printMessage("Transfer to '" + message.username() + "' rejected", ConsoleColors.RED);
    }
}
