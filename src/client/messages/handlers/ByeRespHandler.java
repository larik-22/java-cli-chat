package client.messages.handlers;

import client.connection.ServerConnection;
import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.ByeResp;
import shared.utils.PrintingUtils;

public class ByeRespHandler implements MessageHandler<ByeResp> {
    private final ServerConnection serverConnection;

    public ByeRespHandler(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void handle(ByeResp message) {
        PrintingUtils.printMessage("Bye-bye", ConsoleColors.GREEN);
        serverConnection.closeConnection();
    }
}
