package server.logger;

import server.connection.ClientRegistry;

/**
 * The ServerLogger class is responsible for logging messages to the console. It is used to log messages, heartbeats,
 * client counts, and custom messages.
 */
public class ServerLogger {
    public enum LogType {
        MESSAGE, HEARTBEAT, CLIENT_USER_COUNT, CUSTOM
    }

    public static void log(LogEntry logEntry) {
        String userPart = "(" + (logEntry.getUsername() == null ? "" : logEntry.getUsername()) + ")";
        String color = logEntry.getColor() == null ? "" : logEntry.getColor();
        String resetColor = "\u001B[0m"; // ANSI reset code

        switch (logEntry.getLogType()) {
            case MESSAGE:
                System.out.printf("%s%s:%d %s %s %s %s%n", color, logEntry.getClientAddress(), logEntry.getClientPort(), userPart, logEntry.getDirection(), logEntry.getMessage(), resetColor);
                break;
            case CLIENT_USER_COUNT:
                System.out.printf("%d client(s) / %d user(s)%n", logEntry.getClientCount(), logEntry.getUserCount());
                break;
        }
    }

    /**
     * A helper method to log the client count. It never changes, so it's worth having a helper method for it.
     */
    public static void logClientCount(){
        ServerLogger.log(new LogEntry.Builder()
                .logType(ServerLogger.LogType.CLIENT_USER_COUNT)
                .clientCount(ClientRegistry.getInstance().getTotalClients())
                .userCount(ClientRegistry.getInstance().getLoggedInUsers())
                .build());
    }
}