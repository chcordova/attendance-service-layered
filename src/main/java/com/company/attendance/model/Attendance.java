package com.company.attendance.model;

import com.company.attendance.exception.FutureDateException;
import com.company.attendance.exception.InvalidAttendanceException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity representing an employee attendance mark in the database. Enforces core domain
 * invariants in accordance with layered architecture principles.
 */
@Entity
@Table(name = "attendances")
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AttendanceType type;

    /**
     * Domain constructor enforcing business rules and domain invariants upon creation.
     *
     * @param employeeId Non-null, strictly positive identifier of the employee
     * @param timestamp Non-null timestamp, cannot be in the future
     * @param type Non-null attendance type (CHECK_IN or CHECK_OUT)
     */
    public Attendance(Long employeeId, LocalDateTime timestamp, AttendanceType type) {
        validateDomainInvariants(employeeId, timestamp, type);
        this.employeeId = employeeId;
        this.timestamp = timestamp;
        this.type = type;
    }

    /** Full constructor including database identifier. */
    public Attendance(Long id, Long employeeId, LocalDateTime timestamp, AttendanceType type) {
        this(employeeId, timestamp, type);
        this.id = id;
    }

    /** Lifecycle callback before persisting or updating entity in the database. */
    @PrePersist
    @PreUpdate
    public void onPrePersistOrUpdate() {
        validateDomainInvariants(this.employeeId, this.timestamp, this.type);
    }

    /**
     * Enforces domain invariants for attendance marks. Throws self-contained domain exceptions
     * directly without hardcoded strings.
     */
    public static void validateDomainInvariants(
            Long employeeId, LocalDateTime timestamp, AttendanceType type) {
        if (employeeId == null || employeeId <= 0) {
            throw new InvalidAttendanceException();
        }
        if (timestamp == null) {
            throw new InvalidAttendanceException();
        }
        if (timestamp.isAfter(LocalDateTime.now())) {
            throw new FutureDateException(timestamp);
        }
        if (type == null) {
            throw new InvalidAttendanceException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attendance that = (Attendance) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Attendance{"
                + "id="
                + id
                + ", employeeId="
                + employeeId
                + ", timestamp="
                + timestamp
                + ", type="
                + type
                + '}';
    }
}
