package com.example.eumbev2.common.exception;

/**
 * Unchecked exception carrying an {@link ErrorCode}. Caught by
 * {@link GlobalExceptionHandler} and rendered as the API spec's Error response.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
