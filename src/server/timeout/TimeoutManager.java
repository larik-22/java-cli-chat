package server.timeout;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages timeouts for a session.
 * Uses a {@link ScheduledExecutorService} to schedule a task that will call the {@link TimeoutHandler#onTimeout(Object)} method
 * @param <T> The type of the object (session) that the timeout handler is handling*
 */
public class TimeoutManager<T> {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final T session;
    private final TimeoutHandler<T> timeoutHandler;
    private ScheduledFuture<?> timeoutTask;

    public TimeoutManager(T session, TimeoutHandler<T> timeoutHandler) {
        this.session = session;
        this.timeoutHandler = timeoutHandler;
    }

    /**
     * Start the timeout task
     * @param timeout time amount
     * @param timeUnit time unit
     */
    public void startTimeout(long timeout, TimeUnit timeUnit) {
        timeoutTask = scheduler.schedule(() -> {
            synchronized (session) {
                if (!timeoutHandler.isConditionFulfilled(session)) {
                    timeoutHandler.onTimeout(session);
                }
            }
        }, timeout, timeUnit);
    }

    /**
     * Stop the timeout task and shutdown the scheduler
     */
    public void stop() {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }
        scheduler.shutdown();
    }
}
