package com.yisy.modules.doramcu.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for exception classes.
 */
class ExceptionTest {

    // ---- ErrorResponse Tests ----

    @Test
    @DisplayName("ErrorResponse full constructor sets all fields")
    void testErrorResponseFullConstructor() {
        Instant now = Instant.now();
        ErrorResponse response = new ErrorResponse(404, "Not Found", "Resource missing", "corr-123", now);

        assertEquals(404, response.status());
        assertEquals("Not Found", response.error());
        assertEquals("Resource missing", response.message());
        assertEquals("corr-123", response.correlationId());
        assertEquals(now, response.timestamp());
    }

    @Test
    @DisplayName("ErrorResponse short constructor auto-generates timestamp")
    void testErrorResponseShortConstructor() {
        Instant before = Instant.now();
        ErrorResponse response = new ErrorResponse(500, "Server Error", "Internal failure");
        Instant after = Instant.now();

        assertEquals(500, response.status());
        assertEquals("Server Error", response.error());
        assertEquals("Internal failure", response.message());
        assertNull(response.correlationId());
        assertNotNull(response.timestamp());
        assertTrue(!response.timestamp().isBefore(before) && !response.timestamp().isAfter(after));
    }

    @Test
    @DisplayName("ErrorResponse with correlationId constructor")
    void testErrorResponseWithCorrelationId() {
        ErrorResponse response = new ErrorResponse(401, "Unauthorized", "Invalid token", "corr-abc");

        assertEquals(401, response.status());
        assertEquals("Unauthorized", response.error());
        assertEquals("Invalid token", response.message());
        assertEquals("corr-abc", response.correlationId());
        assertNotNull(response.timestamp());
    }

    @Test
    @DisplayName("ErrorResponse equals and hashCode")
    void testErrorResponseEqualsAndHashCode() {
        Instant ts = Instant.parse("2024-01-01T00:00:00Z");
        ErrorResponse r1 = new ErrorResponse(400, "Bad Request", "Invalid input", "corr-1", ts);
        ErrorResponse r2 = new ErrorResponse(400, "Bad Request", "Invalid input", "corr-1", ts);
        ErrorResponse r3 = new ErrorResponse(500, "Server Error", "Failure", "corr-1", ts);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }

    @Test
    @DisplayName("ErrorResponse toString contains fields")
    void testErrorResponseToString() {
        ErrorResponse response = new ErrorResponse(401, "Unauthorized", "Invalid token", "corr-xyz",
                Instant.parse("2024-01-01T00:00:00Z"));
        String str = response.toString();

        assertTrue(str.contains("401"));
        assertTrue(str.contains("Unauthorized"));
        assertTrue(str.contains("Invalid token"));
        assertTrue(str.contains("corr-xyz"));
    }

    // ---- InvalidPlayerIdException Tests ----

    @Test
    @DisplayName("InvalidPlayerIdException stores message")
    void testInvalidPlayerIdExceptionMessage() {
        InvalidPlayerIdException ex = new InvalidPlayerIdException("Player 99 not found");

        assertEquals("Player 99 not found", ex.getMessage());
    }

    @Test
    @DisplayName("InvalidPlayerIdException extends IllegalArgumentException")
    void testInvalidPlayerIdExceptionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            throw new InvalidPlayerIdException("Bad ID");
        });
    }

    @Test
    @DisplayName("InvalidPlayerIdException with null message")
    void testInvalidPlayerIdExceptionNullMessage() {
        InvalidPlayerIdException ex = new InvalidPlayerIdException(null);
        assertNull(ex.getMessage());
    }

    // ---- NotYourTurnException Tests ----

    @Test
    @DisplayName("NotYourTurnException stores message")
    void testNotYourTurnExceptionMessage() {
        NotYourTurnException ex = new NotYourTurnException("It is player 2's turn");

        assertEquals("It is player 2's turn", ex.getMessage());
    }

    @Test
    @DisplayName("NotYourTurnException extends IllegalStateException")
    void testNotYourTurnExceptionType() {
        assertThrows(IllegalStateException.class, () -> {
            throw new NotYourTurnException("Not your turn");
        });
    }

    @Test
    @DisplayName("NotYourTurnException with null message")
    void testNotYourTurnExceptionNullMessage() {
        NotYourTurnException ex = new NotYourTurnException(null);
        assertNull(ex.getMessage());
    }
}
