package com.company.attendance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.attendance.dto.AttendanceRequestDto;
import com.company.attendance.dto.AttendanceResponseDto;
import com.company.attendance.dto.PaginatedResponseDto;
import com.company.attendance.exception.FutureDateException;
import com.company.attendance.model.AttendanceType;
import com.company.attendance.service.AttendanceService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AttendanceService attendanceService;

    @Test
    @DisplayName(
            "POST /api/v1/attendances - Should return 201 Created with Location header and"
                    + " AttendanceResponseDto")
    void shouldReturn201WhenRegisteringValidAttendance() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2026, 9, 26, 8, 30, 0);
        AttendanceResponseDto responseDto =
                new AttendanceResponseDto(1L, 101L, timestamp, AttendanceType.CHECK_IN);

        when(attendanceService.registerAttendance(any(AttendanceRequestDto.class)))
                .thenReturn(responseDto);

        String jsonPayload =
                """
                {
                    "employeeId": 101,
                    "timestamp": "2026-09-26T08:30:00",
                    "type": "CHECK_IN"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/attendances")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/attendances/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(101))
                .andExpect(jsonPath("$.type").value("CHECK_IN"));

        verify(attendanceService).registerAttendance(any(AttendanceRequestDto.class));
    }

    @Test
    @DisplayName(
            "POST /api/v1/attendances - Should accept UTC ISO timestamp with 'Z' as in Swagger UI")
    void shouldAcceptUtcTimestampWithZ() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2026, 9, 27, 1, 24, 12, 246_000_000);
        AttendanceResponseDto responseDto =
                new AttendanceResponseDto(2L, 102L, timestamp, AttendanceType.CHECK_OUT);

        when(attendanceService.registerAttendance(any(AttendanceRequestDto.class)))
                .thenReturn(responseDto);

        String jsonPayload =
                """
                {
                    "employeeId": 102,
                    "timestamp": "2026-09-27T01:24:12.246Z",
                    "type": "CHECK_OUT"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/attendances")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/attendances/2"))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.employeeId").value(102))
                .andExpect(jsonPath("$.type").value("CHECK_OUT"));
    }

    @Test
    @DisplayName(
            "POST /api/v1/attendances - Should return 400 Bad Request with FUTURE_DATE_NOT_ALLOWED"
                    + " for future date")
    void shouldReturn400WhenFutureDateProvided() throws Exception {
        LocalDateTime future = LocalDateTime.of(2029, 1, 1, 0, 0, 0);
        when(attendanceService.registerAttendance(any(AttendanceRequestDto.class)))
                .thenThrow(new FutureDateException(future));

        String jsonPayload =
                """
                {
                    "employeeId": 101,
                    "timestamp": "2029-01-01T00:00:00",
                    "type": "CHECK_IN"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/attendances")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("FUTURE_DATE_NOT_ALLOWED"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Cannot register an attendance with a future timestamp:"
                                                + " 2029-01-01T00:00"));
    }

    @Test
    @DisplayName(
            "POST /api/v1/attendances - Should return 400 Bad Request with FIELD_VALIDATION_FAILED"
                    + " when fields are invalid")
    void shouldReturn400WhenValidationFails() throws Exception {
        String jsonPayload =
                """
                {
                    "employeeId": -5,
                    "type": null
                }
                """;

        mockMvc.perform(
                        post("/api/v1/attendances")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("FIELD_VALIDATION_FAILED"))
                .andExpect(
                        jsonPath("$.details.employeeId")
                                .value("Employee ID must be a positive number."))
                .andExpect(
                        jsonPath("$.details.timestamp").value("Attendance timestamp is mandatory."))
                .andExpect(
                        jsonPath("$.details.type")
                                .value("Attendance type is mandatory (CHECK_IN or CHECK_OUT)."));
    }

    @Test
    @DisplayName(
            "POST /api/v1/attendances - Should return 400 Bad Request with MALFORMED_JSON_REQUEST"
                    + " for broken JSON")
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        String malformedJson = "{ invalid json content }";

        mockMvc.perform(
                        post("/api/v1/attendances")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON_REQUEST"));
    }

    @Test
    @DisplayName(
            "GET /api/v1/attendances/employee/{employeeId} - Should return 200 OK with"
                    + " PaginatedResponseDto")
    void shouldReturn200WithPaginatedRecords() throws Exception {
        AttendanceResponseDto record1 =
                new AttendanceResponseDto(
                        1L, 101L, LocalDateTime.of(2026, 9, 26, 8, 30, 0), AttendanceType.CHECK_IN);
        PaginatedResponseDto<AttendanceResponseDto> paginatedResponse =
                new PaginatedResponseDto<>(List.of(record1), 0, 10, 1L, 1, true, true);

        when(attendanceService.getAttendancesByEmployee(eq(101L), any()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/v1/attendances/employee/101?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].employeeId").value(101));
    }

    @Test
    @DisplayName(
            "GET /api/v1/attendances/employee/{employeeId} - Should return 200 OK with empty"
                    + " content when no marks exist")
    void shouldReturn200WithEmptyPageWhenNoMarks() throws Exception {
        PaginatedResponseDto<AttendanceResponseDto> emptyResponse =
                new PaginatedResponseDto<>(Collections.emptyList(), 0, 10, 0L, 0, true, true);

        when(attendanceService.getAttendancesByEmployee(eq(999L), any())).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1/attendances/employee/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @DisplayName(
            "GET /api/v1/attendances/employee/{employeeId} - Should return 400 Bad Request if"
                    + " employeeId <= 0")
    void shouldReturn400WhenEmployeeIdIsNonPositive() throws Exception {
        mockMvc.perform(get("/api/v1/attendances/employee/-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_ATTENDANCE_DATA"));

        mockMvc.perform(get("/api/v1/attendances/employee/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_ATTENDANCE_DATA"));
    }

    @Test
    @DisplayName(
            "GET /api/v1/attendances/employee/{employeeId} - Should return 400 Bad Request if page"
                    + " < 0 or size <= 0")
    void shouldReturn400WhenPaginationParamsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/attendances/employee/101?page=-1&size=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Page index must not be less than zero."));

        mockMvc.perform(get("/api/v1/attendances/employee/101?page=0&size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Page size must be greater than zero."));
    }
}
