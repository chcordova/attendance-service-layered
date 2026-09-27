package com.company.attendance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.attendance.exception.FutureDateException;
import com.company.attendance.exception.InvalidAttendanceException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttendanceTest {

    @Test
    @DisplayName(
            "Should successfully construct Attendance entity when all invariants are satisfied")
    void shouldConstructValidAttendance() {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        Attendance attendance = new Attendance(100L, past, AttendanceType.CHECK_IN);

        assertThat(attendance.getEmployeeId()).isEqualTo(100L);
        assertThat(attendance.getTimestamp()).isEqualTo(past);
        assertThat(attendance.getType()).isEqualTo(AttendanceType.CHECK_IN);
    }

    @Test
    @DisplayName("Should construct Attendance entity with ID")
    void shouldConstructValidAttendanceWithId() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(30);
        Attendance attendance = new Attendance(1L, 100L, past, AttendanceType.CHECK_OUT);

        assertThat(attendance.getId()).isEqualTo(1L);
        assertThat(attendance.getEmployeeId()).isEqualTo(100L);
        assertThat(attendance.getTimestamp()).isEqualTo(past);
        assertThat(attendance.getType()).isEqualTo(AttendanceType.CHECK_OUT);
    }

    @Test
    @DisplayName("Should throw InvalidAttendanceException when employeeId is null")
    void shouldThrowWhenEmployeeIdIsNull() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> new Attendance(null, now, AttendanceType.CHECK_IN))
                .isInstanceOf(InvalidAttendanceException.class)
                .hasMessage("Invalid attendance data.");
    }

    @Test
    @DisplayName("Should throw InvalidAttendanceException when employeeId is zero or negative")
    void shouldThrowWhenEmployeeIdIsZeroOrNegative() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> new Attendance(0L, now, AttendanceType.CHECK_IN))
                .isInstanceOf(InvalidAttendanceException.class);

        assertThatThrownBy(() -> new Attendance(-5L, now, AttendanceType.CHECK_IN))
                .isInstanceOf(InvalidAttendanceException.class);
    }

    @Test
    @DisplayName("Should throw InvalidAttendanceException when timestamp is null")
    void shouldThrowWhenTimestampIsNull() {
        assertThatThrownBy(() -> new Attendance(100L, null, AttendanceType.CHECK_IN))
                .isInstanceOf(InvalidAttendanceException.class);
    }

    @Test
    @DisplayName("Should throw FutureDateException when timestamp is in the future")
    void shouldThrowWhenTimestampIsInTheFuture() {
        LocalDateTime future = LocalDateTime.now().plusDays(2);
        assertThatThrownBy(() -> new Attendance(100L, future, AttendanceType.CHECK_IN))
                .isInstanceOf(FutureDateException.class)
                .hasMessageContaining("Cannot register an attendance with a future timestamp");
    }

    @Test
    @DisplayName("Should throw InvalidAttendanceException when type is null")
    void shouldThrowWhenTypeIsNull() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> new Attendance(100L, now, null))
                .isInstanceOf(InvalidAttendanceException.class);
    }

    @Test
    @DisplayName("PrePersist callback should enforce invariants")
    void shouldEnforceInvariantsOnPrePersist() {
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(10L);
        attendance.setTimestamp(LocalDateTime.now().plusHours(5));
        attendance.setType(AttendanceType.CHECK_IN);

        assertThatThrownBy(attendance::onPrePersistOrUpdate)
                .isInstanceOf(FutureDateException.class);
    }

    @Test
    @DisplayName("Should verify equals, hashCode and toString methods")
    void shouldVerifyEqualsAndHashCode() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        Attendance a1 = new Attendance(1L, 100L, past, AttendanceType.CHECK_IN);
        Attendance a2 = new Attendance(1L, 200L, past, AttendanceType.CHECK_OUT);
        Attendance a3 = new Attendance(2L, 100L, past, AttendanceType.CHECK_IN);

        assertThat(a1).isEqualTo(a2);
        assertThat(a1).isNotEqualTo(a3);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        assertThat(a1.toString()).contains("employeeId=100");
    }
}
