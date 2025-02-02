package client.messages.handlers;

import shared.constants.ConsoleColors;
import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.TransferAcceptResp;
import shared.utils.PrintingUtils;

public class TransferAcceptRespHandler implements MessageHandler<TransferAcceptResp> {
    @Override
    public void handle(TransferAcceptResp message) {
        if(message.status().equalsIgnoreCase("OK")){
            PrintingUtils.printMessage("Transfer accept request sent successfully", ConsoleColors.GREEN);
        } else {
            MessageErrorHandler.getInstance().handleError(message.code());
        }
    }
}
