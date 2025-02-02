package client.messages.handlers;

import shared.constants.ConsoleColors;
import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.RPSChoiceResp;
import shared.utils.PrintingUtils;

public class RPSChoiceRespHandler implements MessageHandler<RPSChoiceResp> {
    @Override
    public void handle(RPSChoiceResp message) {
        if(message.status().equalsIgnoreCase("OK")){
            PrintingUtils.printMessage("Choice accepted", ConsoleColors.GREEN);
        } else {
            MessageErrorHandler.getInstance().handleError(message.code());
        }
    }
}
