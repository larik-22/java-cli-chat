package client.messages.handlers;

import shared.constants.ConsoleColors;
import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.BroadcastResp;
import shared.utils.PrintingUtils;

public class BroadcastRespHandler implements MessageHandler<BroadcastResp> {
    @Override
    public void handle(BroadcastResp message) {
        if(message.status().equalsIgnoreCase("ok")){
            PrintingUtils.printMessage("Message sent", ConsoleColors.GREEN);
        } else {
            MessageErrorHandler.getInstance().handleError(message.code());
        }
    }
}
