package com.company.attendance.dto;

import com.company.attendance.model.AttendanceType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Immutable Java 21 Record representing the attendance response sent to clients.
 *
 * @param id Unique record identifier
 * @param employeeId Identifier of the employee
 * @param timestamp Timestamp of the recorded attendance event
 * @param type Type of attendance (CHECK_IN, CHECK_OUT)
 */
public record AttendanceResponseDto(
        Long id,
        Long employeeId,
        @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime timestamp,
        AttendanceType type) {}
