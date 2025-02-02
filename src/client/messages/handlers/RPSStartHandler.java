package client.messages.handlers;

import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.RPSStart;
import shared.utils.PrintingUtils;

public class RPSStartHandler implements MessageHandler<RPSStart> {
    @Override
    public void handle(RPSStart message) {
        String sb = "Rock-paper-scissors game started with: " +
                message.username() +
                System.lineSeparator() +
                "------------------------------------" +
                System.lineSeparator() +
                "Press '6' to choose your move" +
                System.lineSeparator() +
                "------------------------------------";

        PrintingUtils.printMessage(sb, ConsoleColors.YELLOW);
    }
}
