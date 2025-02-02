package client.messages.handlers;

import shared.constants.ConsoleColors;
import shared.messages.handling.MessageHandler;
import shared.messages.model.server.Clients;
import shared.utils.PrintingUtils;

import java.util.List;

public class ClientsHandler implements MessageHandler<Clients> {
    @Override
    public void handle(Clients message) {
        List<String> clients = message.clients();
        String consoleMessage = clients.isEmpty() ? "No clients connected" : "Clients connected: " + String.join(", ", clients);

        if (consoleMessage.equals("No clients connected")) {
            PrintingUtils.printMessage(consoleMessage, ConsoleColors.RED);
        } else {
            PrintingUtils.printMessage(consoleMessage, ConsoleColors.GREEN);
        }
    }
}
