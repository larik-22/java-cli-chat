package shared.messages.handling;

import server.connection.ClientRegistry;
import server.logger.LogEntry;
import server.logger.ServerLogger;
import shared.connection.Connection;
import shared.constants.ConsoleColors;
import shared.constants.ProtocolCommands;
import shared.utils.JsonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketException;

/**
 * The MessageParser class is responsible for parsing incoming messages and invoking the appropriate handler.
 * Can be instantiated as a server or client parser. Based on the type, it will log the incoming messages.
 * Uses provided {@link MessageHandlerRegistry} to retrieve the appropriate handler for the incoming message.
 */
public class MessageParser implements Runnable {
    private final Connection connection;
    private final MessageHandlerRegistry registry;
    private final boolean isServer;

    public MessageParser(Connection connection, MessageHandlerRegistry registry, boolean isServer) {
        this.connection = connection;
        this.registry = registry;
        this.isServer = isServer;
    }

    public MessageParser(Connection connection, MessageHandlerRegistry registry) {
        this(connection, registry, false);
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getSocket().getInputStream()))) {
            String line;

            while (connection.isConnected() && (line = reader.readLine()) != null) {
                // Normalize the line (trim any extra whitespaces)
                String message = line.trim();

                // Ignore empty lines (in case of extra line breaks)
                if (!message.isEmpty()) {
                    handleIncomingMessage(message);
                }
            }
        } catch (SocketException e) {
            if (connection.isConnected()) {
                System.err.println("Lost connection.");
                connection.closeConnection();
            }
        } catch (Exception e) {
            ServerLogger.log(new LogEntry.Builder()
                    .logType(ServerLogger.LogType.MESSAGE)
                    .clientAddress(String.valueOf(connection.getSocket().getInetAddress().getHostAddress()))
                    .clientPort(connection.getSocket().getPort())
                    .message(e.getMessage())
                    .color(ConsoleColors.RED.getColor())
                    .build());
        } finally {
            connection.closeConnection();
        }
    }

    /**
     * Handles an incoming message by parsing it and calling the appropriate handler.
     * Logs the message if the message is incoming from a client.
     *
     * @param message - the raw json message
     */
    private void handleIncomingMessage(String message) throws IOException {
        if (isServer) {
            logMessage(message);
        }

        try {
            // Parse the message into the appropriate object
            Object parsedMessage = JsonUtils.messageToClass(message);

            // Determine the command from the parsed message class
            ProtocolCommands command = JsonUtils.getCommandFromClass(parsedMessage.getClass());

            // Retrieve and invoke the handler
            MessageHandler<?> handler = registry.getHandler(command);
            if (handler != null) {
                MessageHandler<Object> typedHandler = (MessageHandler<Object>) handler;
                typedHandler.handle(parsedMessage);
            } else {
                System.err.println("No handler registered for command: " + command);
            }
        } catch (Exception e) {
            if (e.getMessage() != null && isServer) {
                PrintWriter out = new PrintWriter(connection.getSocket().getOutputStream(), true);
                if (e.getMessage().contains("Unknown command")) {
                    out.println(ProtocolCommands.UNKNOWN_COMMAND);
                } else if (e.getMessage().contains("Failed to parse message content")) {
                    out.println(ProtocolCommands.PARSE_ERROR);
                }
            }
        }
    }

    /**
     * Logs the message to the server log.
     *
     * @param rawMessage - the raw json message
     */
    private void logMessage(String rawMessage) {
        String username = ClientRegistry.getInstance().getUsernameBySocket(connection.getSocket());

        ServerLogger.log(new LogEntry.Builder()
                .logType(ServerLogger.LogType.MESSAGE)
                .clientAddress(String.valueOf(connection.getSocket().getInetAddress().getHostAddress()))
                .clientPort(connection.getSocket().getPort())
                .username(username)
                .direction("-->")
                .message(rawMessage)
                .build());
    }
}
