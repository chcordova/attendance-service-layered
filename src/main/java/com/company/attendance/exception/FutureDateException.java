package com.company.attendance.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when an attendance registration timestamp is in the future. Encapsulates
 * standard default messages to prevent hardcoded error strings across services and entities.
 */
public class FutureDateException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "Cannot register an attendance with a future timestamp.";

    public FutureDateException() {
        super(DEFAULT_MESSAGE);
    }

    public FutureDateException(LocalDateTime timestamp) {
        super("Cannot register an attendance with a future timestamp: " + timestamp);
    }

    public FutureDateException(String message) {
        super(message);
    }
}
