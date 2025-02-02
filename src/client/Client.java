package client;

import client.constants.ClientConfig;
import shared.messages.handling.MessageHandlerRegistry;
import shared.messages.handling.MessageParser;
import client.connection.ServerConnection;
import client.messages.handlers.*;
import shared.constants.ProtocolCommands;

/**
 * The client class is the main class of the client application.
 * It is responsible for creating a connection to the server, registering message handlers,
 * and starting the message parser and user interaction threads.
 * The client class is the entry point of the client application.
 */
public class Client {
    private ServerConnection serverConnection;

    public static void main(String[] args) {
        new Client().run();
    }

    public void run() {
        serverConnection = new ServerConnection(ClientConfig.SERVER_IP, ClientConfig.SERVER_PORT);
        serverConnection.connectToServer();

        // Register message handlers
        MessageHandlerRegistry messageHandlerRegistry = createClientMessageHandlerRegistry(serverConnection);

        // Make sure to close the connection when the application is closed
        // ChatGPT suggested me this solution, although i didn't directly copy it, but adapt.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (serverConnection.isConnected()) {
                serverConnection.sendMessage(ProtocolCommands.BYE.toString());
            }
        }));

        // A new thread is created to handle incoming messages
        new Thread(new MessageParser(serverConnection, messageHandlerRegistry)).start();

        // We could have put UserInteraction in the main thread, but we want to keep the main thread clean.
        new Thread(new UserInteraction(serverConnection)).start();
    }

    /**
     * Create a message handler registry for the client
     * @param serverConnection The server connection
     * @return The message handler registry
     */
    private MessageHandlerRegistry createClientMessageHandlerRegistry(ServerConnection serverConnection) {
        MessageHandlerRegistry messageHandlerRegistry = new MessageHandlerRegistry();
        messageHandlerRegistry.registerHandler(ProtocolCommands.JOINED, new JoinedHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.READY, new ReadyHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.ENTER_RESP, new EnterResponseHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.PING, new PingHandler(serverConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.BROADCAST, new BroadcastHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.BROADCAST_RESP, new BroadcastRespHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.LEFT, new LeftHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.BYE_RESP, new ByeRespHandler(serverConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.HANGUP, new HangupHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.PONG_ERROR, new PongErrorHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.CLIENTS, new ClientsHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.CLIENTS_RESP, new ClientsRespHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.PRIVATE_RESP, new PrivateRespHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.PRIVATE, new PrivateHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.RPS_START, new RPSStartHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.RPS_START_RESP, new RPSStartRespHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.RPS_CHOICE_RESP, new RPSChoiceRespHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.RPS_END, new RPSEndHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.RPS_ERROR, new RpsErrorHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_REQ, new TransferReqHandler(serverConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_RESP, new TransferRespHanlder(serverConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_ACCEPT_RESP, new TransferAcceptRespHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_ACCEPTED, new TransferAcceptedHandler(serverConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_REJECT_RESP, new TransferRejectRespHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_REJECTED, new TransferRejectedHandler());
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_FAILED, new TransferFailedHandler(serverConnection));
        messageHandlerRegistry.registerHandler(ProtocolCommands.TRANSFER_SUCCESS, new TransferSuccessHandler(serverConnection));

        return messageHandlerRegistry;
    }
}
