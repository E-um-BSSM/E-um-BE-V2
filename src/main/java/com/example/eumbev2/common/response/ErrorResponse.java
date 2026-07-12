package com.example.eumbev2.common.response;

import com.example.eumbev2.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Common error body. Mirrors the API spec's `Error` schema
 * (code, message, status, timestamp, path).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        int status,
        Instant timestamp,
        String path
) {

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(
                errorCode.name(),
                message,
                errorCode.getStatus().value(),
                Instant.now(),
                path
        );
    }

    public static ErrorResponse of(String code, int status, String message, String path) {
        return new ErrorResponse(code, message, status, Instant.now(), path);
    }
}
