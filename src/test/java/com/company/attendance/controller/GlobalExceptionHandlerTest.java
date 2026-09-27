package com.company.attendance.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.attendance.dto.ErrorResponseDto;
import com.company.attendance.exception.FutureDateException;
import com.company.attendance.exception.InvalidAttendanceException;
import com.company.attendance.validation.ValidEmployeeId;
import com.company.attendance.validation.ValidPageNumber;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/attendances");
    }

    @Test
    @DisplayName("Should handle FutureDateException returning 400 with FUTURE_DATE_NOT_ALLOWED")
    void shouldHandleFutureDateException() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        FutureDateException ex = new FutureDateException(future);

        ResponseEntity<ErrorResponseDto> response =
                exceptionHandler.handleFutureDateException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FUTURE_DATE_NOT_ALLOWED");
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains(future.toString());
        assertThat(response.getBody().path()).isEqualTo("/api/v1/attendances");
    }

    @Test
    @DisplayName(
            "Should handle InvalidAttendanceException returning 400 with INVALID_ATTENDANCE_DATA")
    void shouldHandleInvalidAttendanceException() {
        InvalidAttendanceException ex = new InvalidAttendanceException();

        ResponseEntity<ErrorResponseDto> response =
                exceptionHandler.handleInvalidAttendanceException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_ATTENDANCE_DATA");
        assertThat(response.getBody().message()).isEqualTo("Invalid attendance data.");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with standard field errors")
    void shouldHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError =
                new FieldError("attendanceRequestDto", "employeeId", "Employee ID is mandatory.");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponseDto> response =
                exceptionHandler.handleMethodArgumentNotValidException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FIELD_VALIDATION_FAILED");
        assertThat(response.getBody().details())
                .containsEntry("employeeId", "Employee ID is mandatory.");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with NotFutureTimestamp error code")
    void shouldHandleMethodArgumentNotValidExceptionWithNotFutureTimestamp() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError =
                new FieldError(
                        "attendanceRequestDto",
                        "timestamp",
                        null,
                        false,
                        new String[] {"NotFutureTimestamp"},
                        null,
                        "Cannot register an attendance with a future timestamp:"
                                + " 2099-01-01T00:00:00");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponseDto> response =
                exceptionHandler.handleMethodArgumentNotValidException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FUTURE_DATE_NOT_ALLOWED");
        assertThat(response.getBody().message())
                .contains("Cannot register an attendance with a future timestamp");
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException with ValidEmployeeId")
    void shouldHandleConstraintViolationExceptionWithValidEmployeeId() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
        ValidEmployeeId annotation = mock(ValidEmployeeId.class);

        when(violation.getConstraintDescriptor()).thenReturn(descriptor);
        when(descriptor.getAnnotation()).thenReturn(annotation);
        when(violation.getMessage()).thenReturn("Employee ID must be a positive number.");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponseDto> response =
                exceptionHandler.handleConstraintViolationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_ATTENDANCE_DATA");
        assertThat(response.getBody().message()).isEqualTo("Invalid attendance data.");
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException with ValidPageNumber")
    void shouldHandleConstraintViolationExceptionWithValidPageNumber() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
        ValidPageNumber annotation = mock(ValidPageNumber.class);

        when(violation.getConstraintDescriptor()).thenReturn(descriptor);
        when(descriptor.getAnnotation()).thenReturn(annotation);
        when(violation.getMessage()).thenReturn("Page index must not be less than zero.");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponseDto> response =
                exceptionHandler.handleConstraintViolationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getBody().message())
                .isEqualTo("Page index must not be less than zero.");
    }

    @Test
    @DisplayName(
            "Should handle generic ConstraintViolationException returning FIELD_VALIDATION_FAILED")
    void shouldHandleGenericConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
        Path path = mock(Path.class);

        when(violation.getConstraintDescriptor()).thenReturn(descriptor);
        when(descriptor.getAnnotation()).thenReturn(mock(java.lang.annotation.Annotation.class));
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("getAttendancesByEmployee.customParam");
        when(violation.getMessage()).thenReturn("Custom violation error.");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponseDto> response =
                exceptionHandler.handleConstraintViolationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FIELD_VALIDATION_FAILED");
        assertThat(response.getBody().details())
                .containsEntry("customParam", "Custom violation error.");
    }

    @Test
    @DisplayName(
            "Should handle HttpMessageNotReadableException returning 400 with"
                    + " MALFORMED_JSON_REQUEST")
    void shouldHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("JSON parse error");

        ResponseEntity<ErrorResponseDto> response =
                exceptionHandler.handleHttpMessageNotReadableException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("MALFORMED_JSON_REQUEST");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException returning 400 with INVALID_REQUEST")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter value");

        ResponseEntity<ErrorResponseDto> response =
                exceptionHandler.handleIllegalArgumentException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("Invalid parameter value");
    }

    @Test
    @DisplayName(
            "Should handle general unhandled Exception returning 500 with INTERNAL_SERVER_ERROR")
    void shouldHandleUnhandledException() {
        Exception ex = new RuntimeException("Unexpected database failure");

        ResponseEntity<ErrorResponseDto> response =
                exceptionHandler.handleUnhandledException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message())
                .isEqualTo("An unexpected internal error occurred. Please contact system support.");
    }
}
