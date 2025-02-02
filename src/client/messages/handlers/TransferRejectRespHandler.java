package client.messages.handlers;

import shared.constants.ConsoleColors;
import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.TransferRejectResp;
import shared.utils.PrintingUtils;

public class TransferRejectRespHandler implements MessageHandler<TransferRejectResp> {
    @Override
    public void handle(TransferRejectResp message) {
        if(message.status().equalsIgnoreCase("OK")){
            PrintingUtils.printMessage("Transfer request rejected successfully", ConsoleColors.GREEN);
        } else {
            MessageErrorHandler.getInstance().handleError(message.code());
        }
    }
}
