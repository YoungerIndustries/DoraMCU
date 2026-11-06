package com.yisy.modules.doramcu.exception;

import com.yisy.modules.doramcu.service.ErrorTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ErrorTracker errorTracker;

    public GlobalExceptionHandler(ErrorTracker errorTracker) {
        this.errorTracker = errorTracker;
    }

    private String getCorrelationId() {
        return MDC.get("correlationId");
    }

    private void trackError(Exception e, WebRequest request) {
        String type = e.getClass().getSimpleName();
        String endpoint = request != null && request.getDescription(false) != null
                ? request.getDescription(false).replaceAll(".*uri=(.*?).*", "$1")
                : null;
        errorTracker.recordError(type, endpoint);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException e, WebRequest request) {
        trackError(e, request);
        log.warn("Resource not found: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                e.getMessage() != null ? e.getMessage() : "Resource not found",
                getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException e, WebRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        log.debug("Resource not found: {}", e.getMessage());
        // SSE EventSource clients only accept text/event-stream, not application/json.
        // Return plain text for these clients to avoid HttpMediaTypeNotAcceptableException.
        String accept = httpRequest.getHeader("Accept");
        if (accept != null && accept.contains("text/event-stream")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "text/plain")
                    .body("event: error\ndata: Resource not found\n\n");
        }
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Resource not found",
                getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NotYourTurnException.class)
    public ResponseEntity<ErrorResponse> handleNotYourTurn(NotYourTurnException e, WebRequest request) {
        trackError(e, request);
        log.warn("Not your turn: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                e.getMessage(),
                getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e, WebRequest request) {
        trackError(e, request);
        log.warn("Illegal state: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                e.getMessage() != null ? e.getMessage() : "Illegal state",
                getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidPlayerIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPlayerId(InvalidPlayerIdException e, WebRequest request) {
        trackError(e, request);
        log.warn("Invalid player ID: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                e.getMessage(),
                getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e, WebRequest request) {
        trackError(e, request);
        log.warn("Bad request: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                e.getMessage() != null ? e.getMessage() : "Invalid argument",
                getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, WebRequest request) {
        trackError(e, request);
        log.warn("Validation failed: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                e.getBindingResult().getFieldErrors().stream()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Validation failed"),
                getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e, WebRequest request) {
        trackError(e, request);
        log.error("Unexpected error: {}", e.getMessage(), e);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
