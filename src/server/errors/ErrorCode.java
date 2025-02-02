package server.errors;

/**
 * This enum represents the error codes that are sent to the client when an error occurs.
 */
public enum ErrorCode {
    USERNAME_TAKEN(5000, "User with this name already exists"),
    INVALID_USERNAME(5001, "Invalid username format"),
    ALREADY_LOGGED_IN(5002, "Already logged in"),
    NOT_LOGGED_IN(6000, "You are not logged in"),
    NO_PONG_RECEIVED(7000, "No pong received"),
    PONG_WITHOUT_PING(8000, "Pong without ping"),
    RECEIVER_NOT_FOUND(9001, "Receiver is not found"),
    INVALID_RECEIVER(9002, "You cannot interact with yourself"),
    TWO_USERS_ALREADY_PLAYING(10002, "Two users already started a game"),
    NO_GAME_ACTIVE(10003, "No game is active"),
    INVALID_RPS_CHOICE(10005, "Invalid rock, paper, scissors choice"),
    ALREADY_MADE_A_CHOICE(10007, "You already made a choice"),
    RESPONSE_TIMEOUT(10008, "Response timeout"),
    USER_UNEXPECTEDLY_DISCONNECTED(10009, "User unexpectedly disconnected"),
    COMMAND_NOT_EXPECTED(12001, "Command not expected"),
    CHECKSUMS_NOT_EQUAL(12002, "Checksums not matching");

    private final int code;
    private final String explanation;

    ErrorCode(int code, String explanation) {
        this.code = code;
        this.explanation = explanation;
    }

    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }

        return null;
    }

    public int getCode() {
        return code;
    }

    public String getExplanation() {
        return explanation;
    }
}
