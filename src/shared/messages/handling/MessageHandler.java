package shared.messages.handling;

/**
 * Interface for handling messages of type T. A consumer could have been used instead, but this would limit the
 * runtime type checking. The MessageHandler interface allows for type-safe handling of messages at runtime.
 * @param <T> - The type of message to handle
 */
public interface MessageHandler<T> {
    void handle(T message);
}
