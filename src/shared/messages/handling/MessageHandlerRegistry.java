package shared.messages.handling;

import shared.constants.ProtocolCommands;

import java.util.HashMap;
import java.util.Map;

/**
 * An utility class that holds the handlers for the messages that the server can handle.
 * Uses {@link ProtocolCommands} as keys and {@link MessageHandler} as values.
 */
public class MessageHandlerRegistry {
    private final Map<ProtocolCommands, MessageHandler<?>> handlers = new HashMap<>();

    public void registerHandler(ProtocolCommands command, MessageHandler<?> handler) {
        handlers.put(command, handler);
    }

    @SuppressWarnings("unchecked")
    public <T> MessageHandler<T> getHandler(ProtocolCommands command) {
        return (MessageHandler<T>) handlers.get(command);
    }
}
