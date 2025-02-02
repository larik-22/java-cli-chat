package server.errors;

/**
 * This class represents an exception that is thrown when the client sends an invalid request.
 * The exception contains an error code that is sent to the client.
 * Uses the {@link ErrorCode} enum to define the error codes.
 */
public class ClientException extends Exception {
    private final ErrorCode errorCode;

    public int getErrorCode() {
        return errorCode.getCode();
    }

    public ClientException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
