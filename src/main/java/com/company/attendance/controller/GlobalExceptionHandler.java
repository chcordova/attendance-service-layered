package com.company.attendance.controller;

import com.company.attendance.dto.ErrorResponseDto;
import com.company.attendance.exception.FutureDateException;
import com.company.attendance.exception.InvalidAttendanceException;
import com.company.attendance.validation.ValidEmployeeId;
import com.company.attendance.validation.ValidPageNumber;
import com.company.attendance.validation.ValidPageSize;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global REST exception handler mapping application exceptions to standardized ErrorResponseDto
 * objects. Located within the controller package as an integral part of the presentation/web
 * delivery layer.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FutureDateException.class)
    public ResponseEntity<ErrorResponseDto> handleFutureDateException(
            FutureDateException ex, HttpServletRequest request) {
        log.warn("Future date violation on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto errorResponse =
                new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "FUTURE_DATE_NOT_ALLOWED",
                        ex.getMessage(),
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidAttendanceException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidAttendanceException(
            InvalidAttendanceException ex, HttpServletRequest request) {
        log.warn(
                "Invalid attendance domain violation on {}: {}",
                request.getRequestURI(),
                ex.getMessage());
        ErrorResponseDto errorResponse =
                new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "INVALID_ATTENDANCE_DATA",
                        ex.getMessage(),
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            if ("NotFutureTimestamp".equals(error.getCode())) {
                log.warn(
                        "Future timestamp validation failed for request {}: {}",
                        request.getRequestURI(),
                        error.getDefaultMessage());
                ErrorResponseDto futureResponse =
                        new ErrorResponseDto(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                "FUTURE_DATE_NOT_ALLOWED",
                                error.getDefaultMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(futureResponse);
            }
        }

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation failed for request {}: {}", request.getRequestURI(), fieldErrors);
        ErrorResponseDto errorResponse =
                new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "FIELD_VALIDATION_FAILED",
                        "Validation failed for one or more fields.",
                        request.getRequestURI(),
                        fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            if (violation.getConstraintDescriptor().getAnnotation() instanceof ValidEmployeeId) {
                log.warn(
                        "Invalid employee ID violation on {}: {}",
                        request.getRequestURI(),
                        violation.getMessage());
                ErrorResponseDto errorResponse =
                        new ErrorResponseDto(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                "INVALID_ATTENDANCE_DATA",
                                "Invalid attendance data.",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        }

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            if (violation.getConstraintDescriptor().getAnnotation() instanceof ValidPageNumber
                    || violation.getConstraintDescriptor().getAnnotation()
                            instanceof ValidPageSize) {
                log.warn(
                        "Pagination parameter violation on {}: {}",
                        request.getRequestURI(),
                        violation.getMessage());
                ErrorResponseDto errorResponse =
                        new ErrorResponseDto(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                "INVALID_REQUEST",
                                violation.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        }

        Map<String, String> details = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String property = violation.getPropertyPath().toString();
            if (property.contains(".")) {
                property = property.substring(property.lastIndexOf('.') + 1);
            }
            details.put(property, violation.getMessage());
        }

        log.warn("Parameter constraint violation on {}: {}", request.getRequestURI(), details);
        ErrorResponseDto errorResponse =
                new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "FIELD_VALIDATION_FAILED",
                        "Validation failed for one or more parameters.",
                        request.getRequestURI(),
                        details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto errorResponse =
                new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "MALFORMED_JSON_REQUEST",
                        "Malformed JSON request or invalid data format.",
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument provided on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto errorResponse =
                new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "INVALID_REQUEST",
                        ex.getMessage(),
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnhandledException(
            Exception ex, HttpServletRequest request) {
        log.error(
                "Unhandled server exception occurred on {}: {}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);
        ErrorResponseDto errorResponse =
                new ErrorResponseDto(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected internal error occurred. Please contact system support.",
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
