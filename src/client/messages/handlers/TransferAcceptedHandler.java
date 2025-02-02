package client.messages.handlers;

import client.connection.ServerConnection;
import client.transfer.ReceiverClient;
import client.transfer.SenderClient;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.TransferAccepted;

import java.io.File;

public class TransferAcceptedHandler implements MessageHandler<TransferAccepted> {
    // only need this to get the filepath
    private final ServerConnection serverConnection;

    public TransferAcceptedHandler(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void handle(TransferAccepted message) {
        // create either a receiver or sender client here and run it
        String role = String.valueOf(message.uuid().charAt(message.uuid().length() - 1)).toUpperCase();
        switch (role) {
            case "S" -> {
                new Thread(new SenderClient(serverConnection, message.uuid(), new File(serverConnection.getFilePath(message.filename())))).start();
            }
            case "R" -> {
                new Thread(new ReceiverClient(serverConnection, message.uuid(), message.filename())).start();
            }
        }
    }
}
