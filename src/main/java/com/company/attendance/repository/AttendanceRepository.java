package com.company.attendance.repository;

import com.company.attendance.model.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for Attendance entity. Provides data persistence and query operations
 * against the 'attendances' database table.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * Finds attendance marks belonging to a specific employee with pagination and sorting.
     *
     * @param employeeId Target employee ID
     * @param pageable Pagination and sorting information
     * @return Page of Attendance entities
     */
    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);
}
