package client.messages.handlers;

import shared.constants.ConsoleColors;
import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.ClientsResp;
import shared.utils.PrintingUtils;

public class ClientsRespHandler implements MessageHandler<ClientsResp> {
    @Override
    public void handle(ClientsResp message) {
        if(!message.status().equalsIgnoreCase("ok")){
            MessageErrorHandler.getInstance().handleError(message.code());
        } else {
            PrintingUtils.printMessage("Message sent", ConsoleColors.GREEN);
        }
    }
}
