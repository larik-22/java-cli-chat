package client.messages.handlers;

import shared.constants.ConsoleColors;
import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.EnterResp;
import shared.utils.PrintingUtils;

public class EnterResponseHandler implements MessageHandler<EnterResp> {
    @Override
    public void handle(EnterResp message) {
        if(message.status().equalsIgnoreCase("ok")){
            PrintingUtils.printMessage("Successfully logged in", ConsoleColors.GREEN);
        } else {
            MessageErrorHandler.getInstance().handleError(message.code());
        }
    }
}
