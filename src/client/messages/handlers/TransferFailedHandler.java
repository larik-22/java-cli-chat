package client.messages.handlers;

import client.connection.ServerConnection;
import shared.constants.ConsoleColors;
import client.MessageErrorHandler;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.TransferFailed;
import shared.utils.PrintingUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TransferFailedHandler implements MessageHandler<TransferFailed> {
    private final ServerConnection serverConnection;

    public TransferFailedHandler(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void handle(TransferFailed message) {
        try {
            // cleanup
            serverConnection.removeSessionId(message.id());

            if (serverConnection.getFilePath(message.filename()) != null) {
                Path path = Paths.get(serverConnection.getFilePath(message.filename()));
                System.out.println("Deleting file: " + path);
                System.out.println(Files.deleteIfExists(path));
            }

            StringBuilder sb = new StringBuilder();

            sb.append("File transfer failed")
                    .append(System.lineSeparator())
                    .append("------------------------------------")
                    .append(System.lineSeparator())
                    .append("File name: ")
                    .append(message.filename())
                    .append(System.lineSeparator())
                    .append("------------------------------------");

            PrintingUtils.printMessage(sb.toString(), ConsoleColors.RED);
            MessageErrorHandler.getInstance().handleError(message.code());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
