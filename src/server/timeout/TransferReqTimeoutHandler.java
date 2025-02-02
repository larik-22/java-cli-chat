package server.timeout;

import server.errors.ErrorCode;
import server.model.TransferConnectionPair;
import server.transfer.FileTransferManager;
import shared.messages.model.server.TransferResp;

/**
 * The TransferReqTimeoutHandler class is responsible for handling the timeout of the transfer request.
 * If the response is not received within the timeout, the handler sends an error message to both the sender and receiver.
 * The handler is used by the {@link server.transfer.FileTransferManager} to handle the timeout of the transfer request.
 */
public class TransferReqTimeoutHandler implements TimeoutHandler<TransferConnectionPair> {
    private final TransferConnectionPair connectionPair;
    private volatile boolean responseReceived = false;

    public TransferReqTimeoutHandler(TransferConnectionPair connectionPair) {
        this.connectionPair = connectionPair;
    }

    @Override
    public boolean isConditionFulfilled(TransferConnectionPair session) {
        return responseReceived;
    }

    @Override
    public void onTimeout(TransferConnectionPair session) {
        if (!responseReceived) {
            TransferResp timeoutResponse = new TransferResp(connectionPair.getSession().getId(), "ERROR", ErrorCode.RESPONSE_TIMEOUT.getCode());
            connectionPair.getSender().sendMessage(timeoutResponse);
            connectionPair.getReceiver().sendMessage(timeoutResponse);
            FileTransferManager.getInstance().removePendingSession(connectionPair.getSender().getUsername(), connectionPair.getReceiver().getUsername());
        }
    }
}
