package client.messages.handlers;

import shared.constants.ConsoleColors;
import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.RPSStartResp;
import shared.utils.PrintingUtils;

public class RPSStartRespHandler implements MessageHandler<RPSStartResp> {
    @Override
    public void handle(RPSStartResp message) {
        if(message.status().equalsIgnoreCase("OK")){
            String sb = "Rock-paper-scissors game started" +
                    System.lineSeparator() +
                    "------------------------------------" +
                    System.lineSeparator() +
                    "Press '6' to choose your move" +
                    System.lineSeparator() +
                    "------------------------------------";

            PrintingUtils.printMessage(sb, ConsoleColors.YELLOW);
        } else {
            MessageErrorHandler.getInstance().handleError(message.code());

            if(message.users() != null){
                PrintingUtils.printMessage("Two users are already playing: " + message.users().get(0) + " and " + message.users().get(1), ConsoleColors.RED);
            }
        }
    }
}
