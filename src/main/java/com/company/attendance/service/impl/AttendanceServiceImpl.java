package com.company.attendance.service.impl;

import com.company.attendance.dto.AttendanceRequestDto;
import com.company.attendance.dto.AttendanceResponseDto;
import com.company.attendance.dto.PaginatedResponseDto;
import com.company.attendance.exception.FutureDateException;
import com.company.attendance.exception.InvalidAttendanceException;
import com.company.attendance.model.Attendance;
import com.company.attendance.repository.AttendanceRepository;
import com.company.attendance.service.AttendanceService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Production-ready implementation of AttendanceService enforcing business rules, transactional
 * boundaries, and logging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    @Override
    @Transactional
    public AttendanceResponseDto registerAttendance(AttendanceRequestDto requestDto) {
        if (requestDto == null) {
            log.warn("Attempted to register attendance with null request body.");
            throw new InvalidAttendanceException();
        }

        if (requestDto.getEmployeeId() == null || requestDto.getEmployeeId() <= 0) {
            log.warn(
                    "Invalid employee ID supplied during attendance registration: {}",
                    requestDto.getEmployeeId());
            throw new InvalidAttendanceException();
        }

        if (requestDto.getTimestamp() == null) {
            log.warn(
                    "Null timestamp supplied during attendance registration for employee: {}",
                    requestDto.getEmployeeId());
            throw new InvalidAttendanceException();
        }

        if (requestDto.getTimestamp().isAfter(LocalDateTime.now())) {
            log.warn(
                    "Future timestamp rejected for employee ID {}: {}",
                    requestDto.getEmployeeId(),
                    requestDto.getTimestamp());
            throw new FutureDateException(requestDto.getTimestamp());
        }

        if (requestDto.getType() == null) {
            log.warn(
                    "Null attendance type supplied for employee ID: {}",
                    requestDto.getEmployeeId());
            throw new InvalidAttendanceException();
        }

        Attendance attendance =
                new Attendance(
                        requestDto.getEmployeeId(),
                        requestDto.getTimestamp(),
                        requestDto.getType());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info(
                "Successfully registered attendance with ID: {} for employee ID: {}, type: {}",
                savedAttendance.getId(),
                savedAttendance.getEmployeeId(),
                savedAttendance.getType());

        return new AttendanceResponseDto(
                savedAttendance.getId(),
                savedAttendance.getEmployeeId(),
                savedAttendance.getTimestamp(),
                savedAttendance.getType());
    }

    @Override
    public PaginatedResponseDto<AttendanceResponseDto> getAttendancesByEmployee(
            Long employeeId, Pageable pageable) {
        if (employeeId == null || employeeId <= 0) {
            log.warn("Invalid employee ID requested for attendance retrieval: {}", employeeId);
            throw new InvalidAttendanceException();
        }

        log.info(
                "Fetching attendances for employee ID: {}, page: {}, size: {}",
                employeeId,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Attendance> page = attendanceRepository.findByEmployeeId(employeeId, pageable);

        Page<AttendanceResponseDto> dtoPage =
                page.map(
                        att ->
                                new AttendanceResponseDto(
                                        att.getId(),
                                        att.getEmployeeId(),
                                        att.getTimestamp(),
                                        att.getType()));

        return PaginatedResponseDto.of(dtoPage);
    }
}
