package client.messages.handlers;

import client.connection.ServerConnection;
import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.TransferSuccess;
import shared.utils.PrintingUtils;

public class TransferSuccessHandler implements MessageHandler<TransferSuccess> {
    private final ServerConnection serverConnection;

    public TransferSuccessHandler(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void handle(TransferSuccess message) {
        serverConnection.removeSessionId(message.id());

        StringBuilder sb = new StringBuilder();
        sb.append("File transfer succeeded: ")
                .append(System.lineSeparator())
                .append("------------------------------------")
                .append(System.lineSeparator())
                .append("File name: ")
                .append(message.filename())
                .append(System.lineSeparator())
                .append("------------------------------------");

        PrintingUtils.printMessage(sb.toString(), ConsoleColors.GREEN);
    }
}
