package com.company.attendance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Immutable Java 21 Record for standardized error responses across the microservice.
 *
 * @param timestamp Time when the error occurred
 * @param status HTTP status code (e.g., 400, 500)
 * @param error HTTP status reason phrase (e.g., "Bad Request")
 * @param code Domain-specific error code (e.g., "FUTURE_DATE_NOT_ALLOWED")
 * @param message Human-readable explanation of the error
 * @param path Request endpoint URI
 * @param details Optional map of field validation details
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        Map<String, String> details) {

    public ErrorResponseDto(int status, String error, String code, String message, String path) {
        this(LocalDateTime.now(), status, error, code, message, path, null);
    }

    public ErrorResponseDto(
            int status,
            String error,
            String code,
            String message,
            String path,
            Map<String, String> details) {
        this(LocalDateTime.now(), status, error, code, message, path, details);
    }
}
