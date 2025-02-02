package server.transfer;

import server.connection.ClientConnection;
import server.connection.ClientRegistry;
import server.constants.ServerConfig;
import server.model.TransferConnectionPair;
import server.model.TransferSession;
import server.timeout.TimeoutManager;
import server.timeout.TransferReqTimeoutHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Manages file transfers between clients.
 * Keeps track of pending and active transfer sessions.
 * Uses {@link TimeoutManager} to handle transfer request timeouts.
 */
public class FileTransferManager {
    private final List<TransferSession> pendingTransfers = Collections.synchronizedList(new ArrayList<>());
    private final List<TransferSession> activeTransfers = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, TimeoutManager<TransferConnectionPair>> timeoutManagers = new ConcurrentHashMap<>();

    private static final FileTransferManager instance = new FileTransferManager();

    private FileTransferManager() {}

    public static FileTransferManager getInstance() {
        return instance;
    }

    /**
     * Add pending transfer session. Adds a timeout manager for the sender to handle transfer request timeout.
     * @param session Transfer session to add
     */
    public void addPendingSession(TransferSession session) {
        pendingTransfers.add(session);

        ClientConnection senderConnection = ClientRegistry.getInstance().getClientByUsername(session.getSender());
        ClientConnection receiverConnection = ClientRegistry.getInstance().getClientByUsername(session.getReceiver());
        TransferConnectionPair connectionPair = new TransferConnectionPair(senderConnection, receiverConnection, session);

        TransferReqTimeoutHandler transferReqTimeoutHandler = new TransferReqTimeoutHandler(connectionPair);
        TimeoutManager<TransferConnectionPair> timeoutManager = new TimeoutManager<>(connectionPair, transferReqTimeoutHandler);

        timeoutManagers.put(session.getId(), timeoutManager);
        timeoutManager.startTimeout(ServerConfig.FILE_TRANSFER_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * Get pending transfer session by sender and receiver username
     * @param sender Sender username
     * @param receiver Receiver username
     * @return Transfer session if found, null otherwise
     */
    public TransferSession getPendingSession(String sender, String receiver) {
        for (TransferSession session : pendingTransfers) {
            if (session.getSender().equals(sender) && session.getReceiver().equals(receiver)) {
                return session;
            }
        }

        return null;
    }

    /**
     * Get pending transfer session by UUID
     * @param id UUID of the pending transfer session
     * @return Transfer session if found, null otherwise
     */
    public TransferSession getPendingSession(String id) {
        return pendingTransfers.stream()
                .filter(session -> session.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get pending transfer sessions by username (sender or receiver)
     * @param username Username of sender or receiver
     * @return List of pending transfer sessions
     */
    public List<TransferSession> getPendingSessionsByUser(String username) {
        return pendingTransfers.stream()
                .filter(session -> session.getSender().equals(username) || session.getReceiver().equals(username))
                .collect(Collectors.toList());
    }

    /**
     * Get active session by username (sender or receiver)
     * @param username Username of sender or receiver
     * @return Transfer session if found, null otherwise
     */
    public List<TransferSession> getActiveSessionsByUser(String username) {
        return activeTransfers.stream()
                .filter(session -> session.getSender().equals(username) || session.getReceiver().equals(username))
                .collect(Collectors.toList());
    }

    /**
     * Create active session from pending session. Removes pending session and clears timeout manager.
     * @param session Pending session to create active session from
     * @return UUID of the created active session
     */
    public void createActiveSession(TransferSession session) {
        activeTransfers.add(session);
    }

    /**
     * Get active session by UUID
     * @param uuid UUID of the active session
     * @return Transfer session if found, null otherwise
     */
    public TransferSession getActiveSession(String uuid) {
        return activeTransfers.stream()
                .filter(session -> session.getId().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Remove active session by UUID
     * @param uuid UUID of the active session
     */
    public void removeActiveSession(String uuid) {
        TransferSession session = getActiveSession(uuid);
        if (session != null) {
            activeTransfers.remove(session);
        }
    }

    /**
     * Remove active session by TransferSession object
     * @param session Transfer session to remove
     */
    public void removeActiveSession(TransferSession session) {
        activeTransfers.remove(session);
    }

    /**
     * Stops timeout manager and removes pending transfer session
     * @param sender Sender username
     * @param receiver Receiver username
     */
    public void removePendingSession(String sender, String receiver) {
        TransferSession session = getPendingSession(sender, receiver);
        if (session != null) {
            timeoutManagers.get(session.getId()).stop();
            timeoutManagers.remove(session.getId());
            pendingTransfers.remove(session);
        }
    }

    /**
     * Check if user is in pending transfer
     * @param username Username of sender or receiver
     * @return True if user is in pending transfer, false otherwise
     */
    public boolean isUserInPendingTransfer(String username) {
        return pendingTransfers.stream().anyMatch(session -> session.getSender().equals(username) || session.getReceiver().equals(username));
    }

    /**
     * Check if user is in active transfer
     * @param username Username of sender or receiver
     * @return True if user is in active transfer, false otherwise
     */
    public boolean isUserInActiveTransfer(String username) {
        return activeTransfers.stream().anyMatch(session -> session.getSender().equals(username) || session.getReceiver().equals(username));
    }

}
