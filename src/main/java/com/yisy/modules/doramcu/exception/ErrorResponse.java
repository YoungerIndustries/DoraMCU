package com.yisy.modules.doramcu.exception;

import java.time.Instant;

public record ErrorResponse(int status, String error, String message, String correlationId, Instant timestamp) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, null);
    }

    public ErrorResponse(int status, String error, String message, String correlationId) {
        this(status, error, message, correlationId, Instant.now());
    }
}
