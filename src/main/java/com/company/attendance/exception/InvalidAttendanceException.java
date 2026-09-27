package com.company.attendance.exception;

/**
 * Exception thrown when domain invariants or attendance data rules are violated. Encapsulates
 * default error messages.
 */
public class InvalidAttendanceException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Invalid attendance data.";

    public InvalidAttendanceException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidAttendanceException(String message) {
        super(message);
    }
}
