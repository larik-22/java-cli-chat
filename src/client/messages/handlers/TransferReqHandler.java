package client.messages.handlers;

import client.connection.ServerConnection;
import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.client.TransferReq;
import shared.utils.PrintingUtils;

public class TransferReqHandler implements MessageHandler<TransferReq> {
    private final ServerConnection serverConnection;

    public TransferReqHandler(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void handle(TransferReq message) {
        serverConnection.addSessionId(message.username(), message.sessionId());
        String sb = "You have received file request from: " +
                message.username() +
                System.lineSeparator() +
                "------------------------------------" +
                System.lineSeparator() +
                "File name: " +
                message.filename() +
                System.lineSeparator() +
                "File size: " +
                message.filesize() +
                System.lineSeparator() +
                "------------------------------------" +
                System.lineSeparator() +
                "To accept the file request, select '8'" +
                System.lineSeparator() +
                "To reject the file request, select '9'";

        PrintingUtils.printMessage(sb, ConsoleColors.YELLOW);
    }
}
