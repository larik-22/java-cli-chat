package client;

import server.errors.ErrorCode;
import shared.constants.ConsoleColors;
import shared.utils.PrintingUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * The MessageErrorHandler class is responsible for handling errors that occur during the communication between the client and the server.
 * It provides a method to handle the error and print the error message to the console.
 */
public class MessageErrorHandler {
    private static final Map<ErrorCode, String> errorMessages = new HashMap<>();
    private static MessageErrorHandler instance;

    static {
        errorMessages.put(ErrorCode.USERNAME_TAKEN, ErrorCode.USERNAME_TAKEN.getExplanation());
        errorMessages.put(ErrorCode.INVALID_USERNAME, ErrorCode.INVALID_USERNAME.getExplanation());
        errorMessages.put(ErrorCode.ALREADY_LOGGED_IN, ErrorCode.ALREADY_LOGGED_IN.getExplanation());
        errorMessages.put(ErrorCode.NOT_LOGGED_IN, ErrorCode.NOT_LOGGED_IN.getExplanation());
        errorMessages.put(ErrorCode.PONG_WITHOUT_PING, ErrorCode.PONG_WITHOUT_PING.getExplanation());
        errorMessages.put(ErrorCode.RECEIVER_NOT_FOUND, ErrorCode.RECEIVER_NOT_FOUND.getExplanation());
        errorMessages.put(ErrorCode.INVALID_RECEIVER, ErrorCode.INVALID_RECEIVER.getExplanation());
        errorMessages.put(ErrorCode.TWO_USERS_ALREADY_PLAYING, ErrorCode.TWO_USERS_ALREADY_PLAYING.getExplanation());
        errorMessages.put(ErrorCode.INVALID_RPS_CHOICE, ErrorCode.INVALID_RPS_CHOICE.getExplanation());
        errorMessages.put(ErrorCode.NO_GAME_ACTIVE, ErrorCode.NO_GAME_ACTIVE.getExplanation());
        errorMessages.put(ErrorCode.ALREADY_MADE_A_CHOICE, ErrorCode.ALREADY_MADE_A_CHOICE.getExplanation());
        errorMessages.put(ErrorCode.RESPONSE_TIMEOUT, ErrorCode.RESPONSE_TIMEOUT.getExplanation());
        errorMessages.put(ErrorCode.USER_UNEXPECTEDLY_DISCONNECTED, ErrorCode.USER_UNEXPECTEDLY_DISCONNECTED.getExplanation());
        errorMessages.put(ErrorCode.COMMAND_NOT_EXPECTED, ErrorCode.COMMAND_NOT_EXPECTED.getExplanation());
        errorMessages.put(ErrorCode.CHECKSUMS_NOT_EQUAL, ErrorCode.CHECKSUMS_NOT_EQUAL.getExplanation());
    }

    private MessageErrorHandler() {}

    public static synchronized MessageErrorHandler getInstance() {
        if (instance == null) {
            instance = new MessageErrorHandler();
        }
        return instance;
    }

    private String getErrorMessage(int code) {
        ErrorCode errorCode = ErrorCode.fromCode(code);
        if (errorCode == null) {
            return "Unknown error";
        }

        return errorMessages.getOrDefault(errorCode, "Unknown error");
    }

    public void handleError(int code) {
        String message = getErrorMessage(code);
        PrintingUtils.printMessage("Error: " + message, ConsoleColors.RED);
    }
}