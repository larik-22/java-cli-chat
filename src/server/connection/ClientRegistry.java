package server.connection;

import server.errors.ClientException;
import server.errors.ErrorCode;
import server.logger.ServerLogger;
import server.model.TransferSession;
import server.rps.GameManager;
import server.transfer.FileTransferManager;
import shared.messages.model.server.RpsError;
import shared.messages.model.server.TransferFailed;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The client registry is responsible for keeping track of all the clients connected to the server.
 */
public class ClientRegistry {
    private final Set<ClientConnection> clients = ConcurrentHashMap.newKeySet();
    private final Map<String, ClientConnection> users = new ConcurrentHashMap<>();

    private static final ClientRegistry instance = new ClientRegistry();

    private ClientRegistry() {
    }

    public synchronized static ClientRegistry getInstance() {
        return instance;
    }

    /**
     * Adds a client to the registry. A client is a connection to a user. Could be a
     * non-logged in user or a logged in user.
     *
     * @param client
     */
    public void addClient(ClientConnection client) {
        clients.add(client);
    }

    /**
     * Logs in a client. If the username is already taken, the client is not logged in.
     * If client is already logged in, the client is not logged in.
     *
     * @param username - the username of the client
     * @param client   - the client to log in
     * @return true if the client was logged in, false otherwise
     */
    public void logInClient(String username, ClientConnection client) throws ClientException {
        // check if client already logged in
        if (users.containsValue(client)) {
            throw new ClientException(ErrorCode.ALREADY_LOGGED_IN);
        }

        // check username validness
        if (username == null || !username.matches("^[a-zA-Z0-9_]{3,14}$")) {
            throw new ClientException(ErrorCode.INVALID_USERNAME);
        }

        // check if username is taken
        if (isUsernameTaken(username)) {
            throw new ClientException(ErrorCode.USERNAME_TAKEN);
        }


        client.setUsername(username);
        users.put(username, client);
        ServerLogger.logClientCount();
    }

    /**
     * Logs out a client.
     *
     * @param client - the client to log out
     */
    public void removeClient(ClientConnection client) {
        GameManager gameManager = GameManager.getInstance();
        FileTransferManager fileTransferManager = FileTransferManager.getInstance();

        handleGameDisconnection(client, gameManager);
        handlePendingTransferDisconnection(client, fileTransferManager);
        handleActiveTransferDisconnection(client, fileTransferManager);

        removeClientFromUsers(client);
        clients.remove(client);
        client.setUsername(null);
    }

    /**
     * Removes a client from the registry.
     *
     * @param client - the client to remove
     */
    private void removeClientFromUsers(ClientConnection client) {
        if (users.containsValue(client)) {
            users.entrySet().removeIf(entry -> entry.getValue().equals(client));
        }
    }

    /**
     * Removes disconnected client from the game session, if the client is in a game.
     *
     * @param client - the client that disconnected
     */
    private void handleGameDisconnection(ClientConnection client, GameManager gameManager) {
        if (gameManager.isUserInGame(client.getUsername())) {
            ClientConnection opponent = getClientByUsername(gameManager.getActiveGame().getOpponent(client.getUsername()));
            opponent.sendMessage(new RpsError(ErrorCode.USER_UNEXPECTEDLY_DISCONNECTED.getCode()));
            gameManager.endGame();
        }
    }

    /**
     * Removes disconnected client from the pending transfer session, if the client is in a pending transfer.
     *
     * @param client - the client that disconnected
     */
    private void handlePendingTransferDisconnection(ClientConnection client, FileTransferManager fileTransferManager) {
        if (fileTransferManager.isUserInPendingTransfer(client.getUsername())) {
            List<TransferSession> sessions = fileTransferManager.getPendingSessionsByUser(client.getUsername());

            for (TransferSession session : sessions) {
                String otherUser = session.getSender().equals(client.getUsername()) ? session.getReceiver() : session.getSender();
                ClientConnection otherClient = getClientByUsername(otherUser);
                otherClient.sendMessage(new TransferFailed(session.getId(), session.getFilename(), ErrorCode.USER_UNEXPECTEDLY_DISCONNECTED.getCode()));
                fileTransferManager.removePendingSession(session.getSender(), session.getReceiver());
            }
        }
    }

    /**
     * Removes disconnected client from the active transfer session, if the client is in an active transfer.
     *
     * @param client - the client that disconnected
     */
    private void handleActiveTransferDisconnection(ClientConnection client, FileTransferManager fileTransferManager) {
        if(fileTransferManager.isUserInActiveTransfer(client.getUsername())){
            List<TransferSession> sessions = fileTransferManager.getActiveSessionsByUser(client.getUsername());
            for (TransferSession session : sessions) {
                String otherUser = session.getSender().equals(client.getUsername()) ? session.getReceiver() : session.getSender();
                ClientConnection otherClient = getClientByUsername(otherUser);
                otherClient.sendMessage(new TransferFailed(session.getId(), session.getFilename(), ErrorCode.USER_UNEXPECTEDLY_DISCONNECTED.getCode()));
                fileTransferManager.removeActiveSession(session);
            }
        }
    }

    /**
     * Checks if a username is already taken.
     *
     * @param username - the username to check
     * @return true if the username is taken, false otherwise
     */
    public boolean isUsernameTaken(String username) {
        return users.containsKey(username);
    }

    /**
     * Gets a client by username.
     *
     * @param username - the username of the client
     * @return the client with the given username
     */
    public ClientConnection getClientByUsername(String username) {
        return users.get(username);
    }

    /**
     * Gets the username of a client by the socket.
     *
     * @param socket - the socket of the client
     * @return the username of the client or null if not found
     */
    public String getUsernameBySocket(Socket socket) {
        for (Map.Entry<String, ClientConnection> entry : users.entrySet()) {
            if (entry.getValue().getSocket().equals(socket)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Gets all the clients in the registry.
     *
     * @return a list of all the clients
     */
    public Map<String, ClientConnection> getLoggedInClients() {
        return Map.copyOf(users);
    }

    /**
     * Gets all the clients in the registry.
     *
     * @return a list of all the clients
     */
    public Set<ClientConnection> getClients() {
        return Set.copyOf(clients);
    }

    /**
     * Gets the total number of clients in the registry.
     *
     * @return
     */
    public int getTotalClients() {
        return clients.size();
    }

    /**
     * Gets the total number of logged in users in the registry.
     *
     * @return
     */
    public int getLoggedInUsers() {
        return users.size();
    }
}
