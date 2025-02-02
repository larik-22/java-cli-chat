package client.messages.handlers;

import client.connection.ServerConnection;
import shared.constants.ConsoleColors;
import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.TransferResp;
import shared.utils.PrintingUtils;

public class TransferRespHanlder implements MessageHandler<TransferResp> {
    private final ServerConnection serverConnection;

    public TransferRespHanlder(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void handle(TransferResp message) {
        if(message.status().equalsIgnoreCase("OK")){
            PrintingUtils.printMessage("Transfer request sent successfully", ConsoleColors.GREEN);
        } else {
            if(message.sessionId() != null){
                serverConnection.removeSessionId(message.sessionId());
            }
            MessageErrorHandler.getInstance().handleError(message.code());
        }
    }
}
