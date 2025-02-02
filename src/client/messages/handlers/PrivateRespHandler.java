package client.messages.handlers;

import shared.constants.ConsoleColors;
import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.PrivateResp;
import shared.utils.PrintingUtils;

public class PrivateRespHandler implements MessageHandler<PrivateResp> {
    @Override
    public void handle(PrivateResp message) {
        // handle
        if(!message.status().equalsIgnoreCase("ok")){
            MessageErrorHandler.getInstance().handleError(message.code());
        } else {
            PrintingUtils.printMessage("Message sent", ConsoleColors.GREEN);
        }
    }
}
