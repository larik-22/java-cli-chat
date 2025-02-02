package server.logger;

/**
 * Represents a log entry in the server log.
 * Can build a log entry with a builder pattern.
 */
public class LogEntry {
    private final ServerLogger.LogType logType;
    private final String clientAddress;
    private final int clientPort;
    private final String username;
    private final String direction;
    private final String color;
    private final String message;
    private final int clientCount;
    private final int userCount;

    private LogEntry(Builder builder) {
        this.logType = builder.logType;
        this.clientAddress = builder.clientAddress;
        this.clientPort = builder.clientPort;
        this.username = builder.username;
        this.direction = builder.direction;
        this.color = builder.color;
        this.message = builder.message;
        this.clientCount = builder.clientCount;
        this.userCount = builder.userCount;
    }

    public static class Builder {
        private ServerLogger.LogType logType;
        private String clientAddress;
        private int clientPort;
        private String username;
        private String direction;
        private String color;
        private String message;
        private int clientCount;
        private int userCount;

        public Builder logType(ServerLogger.LogType logType) {
            this.logType = logType;
            return this;
        }

        public Builder clientAddress(String clientAddress) {
            this.clientAddress = clientAddress;
            return this;
        }

        public Builder clientPort(int clientPort) {
            this.clientPort = clientPort;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder clientCount(int clientCount) {
            this.clientCount = clientCount;
            return this;
        }

        public Builder userCount(int userCount) {
            this.userCount = userCount;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public LogEntry build() {
            return new LogEntry(this);
        }
    }

    public ServerLogger.LogType getLogType() {
        return logType;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getUsername() {
        return username;
    }

    public String getDirection() {
        return direction;
    }

    public String getMessage() {
        return message;
    }

    public int getClientCount() {
        return clientCount;
    }

    public int getUserCount() {
        return userCount;
    }

    public String getColor() {
        return color;
    }
}