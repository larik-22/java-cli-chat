package client;

import client.connection.ServerConnection;
import shared.constants.ConsoleColors;
import shared.constants.ProtocolCommands;
import shared.messages.model.client.*;
import shared.utils.FileTransferUtils;
import shared.utils.InputUtils;
import shared.utils.JsonUtils;
import shared.utils.PrintingUtils;

import java.io.File;
import java.util.List;

/**
 * Handles user interaction in the CLI application
 * Uses {@link ServerConnection} for server communication
 */
public class UserInteraction implements Runnable {
    private final List<String> MENU_OPTIONS = List.of("Log in", "Broadcast", "Show users", "Private Message", "Start RPS", "Send rps choice", "Start file transfer", "Accept file transfer", "Reject file transfer");
    private final ServerConnection serverConnection;

    public UserInteraction(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    /**
     * Shows the help menu
     */
    private void showHelpMenu() {
        System.out.println("Options:");
        System.out.println("Type 'help' to show this menu");
        // 0 number for "leave" and always in the end
        MENU_OPTIONS.stream().map(option -> MENU_OPTIONS.indexOf(option) + 1 + ". " + option).forEach(System.out::println);
        System.out.println("0. Leave");
    }

    /**
     * Starts the CLI application
     */
    public void run() {
        // show menu once on start
        showHelpMenu();

        while (serverConnection.isConnected()) {
            int choice = selectMenuOption(MENU_OPTIONS);
            switch (choice) {
                case -1 -> showHelpMenu();
                case 0 -> serverConnection.sendMessage(ProtocolCommands.BYE.toString());
                case 1 -> logIn();
                case 2 -> sendBroadcast();
                case 3 -> requestClients();
                case 4 -> sendPrivate();
                case 5 -> startRPS();
                case 6 -> sendRPSChoice();
                case 7 -> startFileTransfer();
                case 8 -> acceptFileTransfer();
                case 9 -> rejectFileTransfer();
                default -> System.err.println("Invalid choice");
            }
        }
    }

    /**
     * Starts the file transfer process by sending a TransferReq message.
     * Asks for recipient and file path.
     * Validates the file and calculates its size and checksum.
     * Sends the TransferReq message.
     * Saves the file path in the server connection for later use.
     */
    private void startFileTransfer() {
        requestClients();
        String recipient = InputUtils.askForNonBlankText("Enter recipient: ");
        String filePath = InputUtils.askForNonBlankText("Enter file path: ");

        // validate file and get file's name and size and checksum
        File file = new File(filePath);
        if (!file.exists()) {
            PrintingUtils.printMessage("File does not exist", ConsoleColors.RED);
            return;
        }

        double fileSizeMB = Double.parseDouble(String.format("%.3f", file.length() / (1024.0 * 1024.0)));

        String checksum = FileTransferUtils.calculateChecksum(file);

        // send file transfer request
        TransferReq transferReq = new TransferReq(recipient, file.getName(), fileSizeMB, checksum, null);
        serverConnection.addFile(file.getName(), filePath);
        serverConnection.sendMessage(JsonUtils.classToMessage(transferReq));
    }

    /**
     * Selects a menu option. Does not print options.
     *
     * @param options the options to select from
     * @return the selected option or -1 if the user typed 'help'
     */
    private int selectMenuOption(List<String> options) {
        int choice = InputUtils.askForNumber("Select an option: ", "help");

        if (choice == -1) {
            return -1;
        }

        while (choice < 0 || choice > options.size()) {
            System.err.println("Invalid choice");
            choice = InputUtils.askForNumber("Select an option: ", "help");

            if (choice == -1) {
                return -1;
            }
        }

        return choice;
    }

    /**
     * Logs in the user by sending an Enter message
     */
    private void logIn() {
        String username = InputUtils.askForNonBlankText("Enter username: ");
        Enter enter = new Enter(username);
        serverConnection.sendMessage((JsonUtils.classToMessage(enter)));
    }

    /**
     * Broadcasts a message to all users by sending a BroadcastReq message
     */
    private void sendBroadcast() {
        String message = InputUtils.askForNonBlankText("Enter message: ");
        BroadcastReq broadcastReq = new BroadcastReq(message);
        serverConnection.sendMessage(JsonUtils.classToMessage(broadcastReq));
    }

    /**
     * Prints available clients and prompts to send a private message with selected recipient
     */
    private void sendPrivate() {
        requestClients();
        String recipient = InputUtils.askForNonBlankText("Enter recipient: ");
        String message = InputUtils.askForNonBlankText("Enter message: ");
        PrivateReq privateReq = new PrivateReq(recipient, message);
        serverConnection.sendMessage(JsonUtils.classToMessage(privateReq));
    }

    /**
     * Requests the list of clients from the server
     */
    private void requestClients() {
        serverConnection.sendMessage(ProtocolCommands.CLIENTS_REQ.toString());
    }

    /**
     * Starts a Rock-Paper-Scissors game by sending a RPSStartReq message
     */
    private void startRPS() {
        requestClients();
        String recipient = InputUtils.askForNonBlankText("Enter recipient: ");
        serverConnection.sendMessage(JsonUtils.classToMessage(new RPSStartReq(recipient)));
    }

    /**
     * Sends the user's choice for Rock-Paper-Scissors game
     */
    private void sendRPSChoice() {
        String choice = InputUtils.askForNonBlankText("Enter choice: ", List.of("rock", "paper", "scissors"));
        serverConnection.sendMessage(JsonUtils.classToMessage(new RPSChoiceReq(choice.toLowerCase())));
    }

    /**
     * Accepts a file transfer request. If there are multiple sessions for the sender, accepts all of them.
     * If there are no sessions for the sender, prints an error message.
     */
    private void acceptFileTransfer() {
        try {
            String sender = InputUtils.askForNonBlankText("Enter sender: ");
            List<String> sessionIds = serverConnection.getSessionIds(sender);
            if (sessionIds == null || sessionIds.isEmpty()) {
                PrintingUtils.printMessage("No session found for sender: " + sender, ConsoleColors.RED);
                return;
            }
            for (String sessionId : sessionIds) {
                TransferAccept transferAccept = new TransferAccept(sessionId);
                serverConnection.sendMessage(JsonUtils.classToMessage(transferAccept));
            }
        } catch (Exception e) {
            PrintingUtils.printMessage("Error accepting file transfer: " + e.getMessage(), ConsoleColors.RED);
        }
    }

    /**
     * Rejects a file transfer request. If there are multiple sessions for the sender, rejects all of them.
     * If there are no sessions for the sender, prints an error message.
     */
    private void rejectFileTransfer() {
        try {
            String sender = InputUtils.askForNonBlankText("Enter sender: ");
            List<String> sessionIds = serverConnection.getSessionIds(sender);
            if (sessionIds == null || sessionIds.isEmpty()) {
                PrintingUtils.printMessage("No session found for sender: " + sender, ConsoleColors.RED);
                return;
            }
            for (String sessionId : sessionIds) {
                TransferReject transferReject = new TransferReject(sessionId);
                serverConnection.sendMessage(JsonUtils.classToMessage(transferReject));
            }
        } catch (Exception e) {
            PrintingUtils.printMessage("Error rejecting file transfer: " + e.getMessage(), ConsoleColors.RED);
        }
    }
}
