package com.company.attendance.dto;

import com.company.attendance.model.AttendanceType;
import com.company.attendance.validation.NotFutureTimestamp;
import com.company.attendance.validation.ValidEmployeeId;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating an employee attendance record. Validates fields with Jakarta
 * Bean Validation constraints and custom business validators.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequestDto {

    @NotNull(message = "Employee ID is mandatory.")
    @ValidEmployeeId
    private Long employeeId;

    @NotNull(message = "Attendance timestamp is mandatory.")
    @NotFutureTimestamp
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;

    @NotNull(message = "Attendance type is mandatory (CHECK_IN or CHECK_OUT).")
    private AttendanceType type;
}
