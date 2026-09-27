package com.company.attendance.service;

import com.company.attendance.dto.AttendanceRequestDto;
import com.company.attendance.dto.AttendanceResponseDto;
import com.company.attendance.dto.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

/** Service interface for attendance management operations. */
public interface AttendanceService {

    /**
     * Registers a new attendance mark for an employee.
     *
     * @param requestDto Attendance registration data transfer object
     * @return Created attendance record response
     */
    AttendanceResponseDto registerAttendance(AttendanceRequestDto requestDto);

    /**
     * Retrieves paginated attendance marks for a given employee.
     *
     * @param employeeId Employee ID
     * @param pageable Pagination parameters
     * @return Paginated response containing attendance DTOs
     */
    PaginatedResponseDto<AttendanceResponseDto> getAttendancesByEmployee(
            Long employeeId, Pageable pageable);
}
