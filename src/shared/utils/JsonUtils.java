package shared.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import shared.constants.ProtocolCommands;
import shared.messages.model.client.*;
import shared.messages.model.server.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for converting messages to classes and vice versa.
 */
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<ProtocolCommands, Class<?>> commandToClassMapping = new HashMap<>();

    static {
        commandToClassMapping.put(ProtocolCommands.READY, Ready.class);
        commandToClassMapping.put(ProtocolCommands.JOINED, Joined.class);
        commandToClassMapping.put(ProtocolCommands.ENTER, Enter.class);
        commandToClassMapping.put(ProtocolCommands.ENTER_RESP, EnterResp.class);
        commandToClassMapping.put(ProtocolCommands.BROADCAST, Broadcast.class);
        commandToClassMapping.put(ProtocolCommands.BROADCAST_REQ, BroadcastReq.class);
        commandToClassMapping.put(ProtocolCommands.BROADCAST_RESP, BroadcastResp.class);
        commandToClassMapping.put(ProtocolCommands.LEFT, Left.class);
        commandToClassMapping.put(ProtocolCommands.PING, Ping.class);
        commandToClassMapping.put(ProtocolCommands.PONG, Pong.class);
        commandToClassMapping.put(ProtocolCommands.PONG_ERROR, PongError.class);
        commandToClassMapping.put(ProtocolCommands.BYE, Bye.class);
        commandToClassMapping.put(ProtocolCommands.BYE_RESP, ByeResp.class);
        commandToClassMapping.put(ProtocolCommands.HANGUP, Hangup.class);
        commandToClassMapping.put(ProtocolCommands.CLIENTS, Clients.class);
        commandToClassMapping.put(ProtocolCommands.CLIENTS_REQ, ClientsReq.class);
        commandToClassMapping.put(ProtocolCommands.CLIENTS_RESP, ClientsResp.class);
        commandToClassMapping.put(ProtocolCommands.PRIVATE, Private.class);
        commandToClassMapping.put(ProtocolCommands.PRIVATE_REQ, PrivateReq.class);
        commandToClassMapping.put(ProtocolCommands.PRIVATE_RESP, PrivateResp.class);
        commandToClassMapping.put(ProtocolCommands.RPS_START_REQ, RPSStartReq.class);
        commandToClassMapping.put(ProtocolCommands.RPS_START_RESP, RPSStartResp.class);
        commandToClassMapping.put(ProtocolCommands.RPS_START, RPSStart.class);
        commandToClassMapping.put(ProtocolCommands.RPS_CHOICE_REQ, RPSChoiceReq.class);
        commandToClassMapping.put(ProtocolCommands.RPS_CHOICE_RESP, RPSChoiceResp.class);
        commandToClassMapping.put(ProtocolCommands.RPS_END, RPSEnd.class);
        commandToClassMapping.put(ProtocolCommands.RPS_ERROR, RpsError.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_REQ, TransferReq.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_RESP, TransferResp.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_ACCEPT, TransferAccept.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_ACCEPT_RESP, TransferAcceptResp.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_ACCEPTED, TransferAccepted.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_REJECTED, TransferRejected.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_REJECT, TransferReject.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_REJECT_RESP, TransferRejectResp.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_CHECKSUM, TransferChecksum.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_SUCCESS, TransferSuccess.class);
        commandToClassMapping.put(ProtocolCommands.TRANSFER_FAILED, TransferFailed.class);
    }

    /**
     * Get the protocol command from a class
     * @param clazz Class to get the command from
     * @return Protocol command or throws an exception if not found
     */
    public static ProtocolCommands getCommandFromClass(Class<?> clazz) {
        return commandToClassMapping.entrySet().stream()
                .filter(entry -> entry.getValue().equals(clazz))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find command for class " + clazz.getName()));
    }

    /**
     * Get the class from a protocol command
     * @param command Protocol command to get the class from
     * @return Class or null if not found
     */
    public static Class<?> getClassFromCommand(ProtocolCommands command) {
        return commandToClassMapping.get(command);
    }

    /**
     * Convert a message string to a specific message class.
     * Allows empty payload.
     * @param message Message string in the format <command> <json payload>
     * @return Message object
     * @param <T> Type of the message object
     */
    public static <T> T messageToClass(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }

        // Determine command and content
        String commandStr;
        String content;

        int firstSpace = message.indexOf(' ');
        if (firstSpace == -1) {
            // No space found, treat the message as just the command
            commandStr = message.trim();
            content = "{}"; // Default to empty JSON object
        } else {
            // Split into command and payload
            commandStr = message.substring(0, firstSpace).trim();
            content = message.substring(firstSpace + 1).trim();
        }

        // Ensure content is valid JSON (default to empty JSON object if blank)
        if (content.isBlank()) {
            content = "{}";
        }

        // Validate the command and get the corresponding class
        ProtocolCommands command;
        try {
            command = ProtocolCommands.valueOf(commandStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown command: " + commandStr);
        }

        Class<?> clazz = getClassFromCommand(command);
        if (clazz == null) {
            throw new RuntimeException("Cannot find class for command: " + command);
        }

        try {
            // Parse JSON payload into the corresponding class
            return (T) objectMapper.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse message content: " + e.getMessage());
        }
    }


    /**
     * Convert an object to a message string following the protocol message format:
     * <command> <json payload>
     * @param obj Object to convert
     * @return Message string in the format <command> <json payload>
     */
    public static String classToMessage(Object obj) {
        Class<?> clazz = obj.getClass();
        ProtocolCommands command = getCommandFromClass(clazz);

        try {
            String content = objectMapper.writeValueAsString(obj);
            return command.toString() + " " + content;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
