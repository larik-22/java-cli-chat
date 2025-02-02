package server.timeout;

/**
 * Interface for handling timeouts.
 * @param <T> The type of the object (session) that the timeout handler is handling.
 */
public interface TimeoutHandler<T> {
    boolean isConditionFulfilled(T session);
    void onTimeout(T session);
}