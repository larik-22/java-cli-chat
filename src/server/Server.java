package server;

import server.connection.ClientConnection;
import server.constants.ServerConfig;
import server.logger.ServerLogger;
import server.messages.handlers.*;
import server.pingpong.PingPongManager;
import server.transfer.FileTransferServer;
import shared.constants.ProtocolCommands;
import shared.messages.handling.MessageHandlerRegistry;
import shared.messages.handling.MessageParser;
import shared.messages.model.server.Ready;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The server class is responsible for starting the server and handling incoming connections.
 * This represents entry point of the server application. This is only messaging server,
 * the {@link server.transfer.FileTransferServer} is started in a separate thread.
 */
public class Server {
    public static void main(String[] args) {new Server().start();}

    public void start() {
        new Thread(new FileTransferServer(ServerConfig.FILE_TRANSFER_PORT)).start();

        try (ServerSocket serverSocket = new ServerSocket(ServerConfig.SERVER_PORT)) {
            while (serverSocket.isBound()) {
                Socket clientSocket = serverSocket.accept();

                // Create Connection instance
                ClientConnection clientConnection = new ClientConnection(clientSocket);

                // Create PingPongManager instance for each client (Unfinished)
                PingPongManager pingPongManager = new PingPongManager(clientConnection);

                // Create handlers specific to the client
                MessageHandlerRegistry messageHandlerRegistry = createServerMessageHandlerRegistry(clientConnection, pingPongManager);
                new Thread((new MessageParser(clientConnection, messageHandlerRegistry, true))).start();

                // Send the client a ready message
                clientConnection.sendMessage(new Ready(String.valueOf(ServerConfig.VERSION)));

                // Log when client connects
                ServerLogger.logClientCount();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    /**
     * Creates a message handler registry for the server and register the handlers for the commands that the server.
     *
     * @param clientConnection – connection instance of the client
     * @param pingPongManager  –  an instance of ping pong handler for the client connection
     */
    public MessageHandlerRegistry createServerMessageHandlerRegistry(ClientConnection clientConnection, PingPongManager pingPongManager) {
        MessageHandlerRegistry messageHandlerRegistry = new MessageHandlerRegistry();
        messageHandlerRegistry.registerHandler(ProtocolCommands.ENTER, new EnterHandler(clientConnection, pingPongManager));
        messageHandlerRegistry.registerHandler(ProtocolCommands.BROADCAST_REQ, new BroadcastReqHandler(clientConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.BYE, new ByeHandler(clientConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.PONG, new PongHandler(pingPongManager));
        messageHandlerRegistry.registerHandler(ProtocolCommands.CLIENTS_REQ, new ClientsReqHandler(clientConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.PRIVATE_REQ, new PrivateReqHandler(clientConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.RPS_START_REQ, new RPSStartReqHandler(clientConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.RPS_CHOICE_REQ, new RPSChoiceReqHandler(clientConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_REQ, new TransferReqHandler(clientConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_ACCEPT, new TransferAcceptHandler(clientConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_REJECT, new TransferRejectHandler(clientConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_CHECKSUM, new TransferChecksumHandler(clientConnection));
        return messageHandlerRegistry;
    }
}
