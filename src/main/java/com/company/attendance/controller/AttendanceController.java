package com.company.attendance.controller;

import com.company.attendance.dto.AttendanceRequestDto;
import com.company.attendance.dto.AttendanceResponseDto;
import com.company.attendance.dto.PaginatedResponseDto;
import com.company.attendance.service.AttendanceService;
import com.company.attendance.validation.ValidEmployeeId;
import com.company.attendance.validation.ValidPageNumber;
import com.company.attendance.validation.ValidPageSize;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST Controller exposing attendance management endpoints under /api/v1/attendances. Implements
 * the contract defined in openapi.yaml using declarative input validation.
 */
@RestController
@RequestMapping("/api/v1/attendances")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceResponseDto> registerAttendance(
            @Valid @RequestBody AttendanceRequestDto requestDto) {
        log.info(
                "Incoming request to register attendance: employeeId={}, type={}",
                requestDto.getEmployeeId(),
                requestDto.getType());

        AttendanceResponseDto response = attendanceService.registerAttendance(requestDto);

        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(response.id())
                        .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<PaginatedResponseDto<AttendanceResponseDto>> getAttendancesByEmployee(
            @PathVariable @ValidEmployeeId Long employeeId,
            @RequestParam(defaultValue = "0") @ValidPageNumber int page,
            @RequestParam(defaultValue = "10") @ValidPageSize int size) {
        log.info(
                "Incoming request to fetch attendances for employeeId={}, page={}, size={}",
                employeeId,
                page,
                size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        PaginatedResponseDto<AttendanceResponseDto> result =
                attendanceService.getAttendancesByEmployee(employeeId, pageable);

        return ResponseEntity.ok(result);
    }
}
