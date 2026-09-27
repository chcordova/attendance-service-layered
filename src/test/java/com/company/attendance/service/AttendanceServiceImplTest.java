package com.company.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.attendance.dto.AttendanceRequestDto;
import com.company.attendance.dto.AttendanceResponseDto;
import com.company.attendance.dto.PaginatedResponseDto;
import com.company.attendance.exception.FutureDateException;
import com.company.attendance.exception.InvalidAttendanceException;
import com.company.attendance.model.Attendance;
import com.company.attendance.model.AttendanceType;
import com.company.attendance.repository.AttendanceRepository;
import com.company.attendance.service.impl.AttendanceServiceImpl;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock private AttendanceRepository attendanceRepository;

    @InjectMocks private AttendanceServiceImpl attendanceService;

    @Test
    @DisplayName("Should successfully register valid attendance mark")
    void shouldRegisterValidAttendance() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(10);
        AttendanceRequestDto request =
                AttendanceRequestDto.builder()
                        .employeeId(101L)
                        .timestamp(past)
                        .type(AttendanceType.CHECK_IN)
                        .build();

        Attendance savedEntity = new Attendance(1L, 101L, past, AttendanceType.CHECK_IN);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedEntity);

        AttendanceResponseDto response = attendanceService.registerAttendance(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.employeeId()).isEqualTo(101L);
        assertThat(response.timestamp()).isEqualTo(past);
        assertThat(response.type()).isEqualTo(AttendanceType.CHECK_IN);

        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw InvalidAttendanceException when request DTO is null")
    void shouldThrowWhenRequestDtoIsNull() {
        assertThatThrownBy(() -> attendanceService.registerAttendance(null))
                .isInstanceOf(InvalidAttendanceException.class);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidAttendanceException when employeeId is null")
    void shouldThrowWhenEmployeeIdIsNull() {
        AttendanceRequestDto request =
                AttendanceRequestDto.builder()
                        .employeeId(null)
                        .timestamp(LocalDateTime.now())
                        .type(AttendanceType.CHECK_IN)
                        .build();

        assertThatThrownBy(() -> attendanceService.registerAttendance(request))
                .isInstanceOf(InvalidAttendanceException.class);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidAttendanceException when employeeId is negative or zero")
    void shouldThrowWhenEmployeeIdIsZeroOrNegative() {
        AttendanceRequestDto requestZero =
                AttendanceRequestDto.builder()
                        .employeeId(0L)
                        .timestamp(LocalDateTime.now())
                        .type(AttendanceType.CHECK_IN)
                        .build();

        assertThatThrownBy(() -> attendanceService.registerAttendance(requestZero))
                .isInstanceOf(InvalidAttendanceException.class);

        AttendanceRequestDto requestNegative =
                AttendanceRequestDto.builder()
                        .employeeId(-10L)
                        .timestamp(LocalDateTime.now())
                        .type(AttendanceType.CHECK_IN)
                        .build();

        assertThatThrownBy(() -> attendanceService.registerAttendance(requestNegative))
                .isInstanceOf(InvalidAttendanceException.class);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidAttendanceException when timestamp is null")
    void shouldThrowWhenTimestampIsNull() {
        AttendanceRequestDto request =
                AttendanceRequestDto.builder()
                        .employeeId(101L)
                        .timestamp(null)
                        .type(AttendanceType.CHECK_IN)
                        .build();

        assertThatThrownBy(() -> attendanceService.registerAttendance(request))
                .isInstanceOf(InvalidAttendanceException.class);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw FutureDateException when timestamp is in the future")
    void shouldThrowWhenTimestampIsInTheFuture() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        AttendanceRequestDto request =
                AttendanceRequestDto.builder()
                        .employeeId(101L)
                        .timestamp(future)
                        .type(AttendanceType.CHECK_IN)
                        .build();

        assertThatThrownBy(() -> attendanceService.registerAttendance(request))
                .isInstanceOf(FutureDateException.class)
                .hasMessageContaining("Cannot register an attendance with a future timestamp");

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidAttendanceException when attendance type is null")
    void shouldThrowWhenTypeIsNull() {
        AttendanceRequestDto request =
                AttendanceRequestDto.builder()
                        .employeeId(101L)
                        .timestamp(LocalDateTime.now().minusMinutes(5))
                        .type(null)
                        .build();

        assertThatThrownBy(() -> attendanceService.registerAttendance(request))
                .isInstanceOf(InvalidAttendanceException.class);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve paginated attendance records for employee")
    void shouldRetrievePaginatedAttendances() {
        Long employeeId = 101L;
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now().minusHours(2);
        Attendance a1 = new Attendance(1L, employeeId, now, AttendanceType.CHECK_IN);
        Attendance a2 = new Attendance(2L, employeeId, now.plusHours(1), AttendanceType.CHECK_OUT);
        Page<Attendance> page = new PageImpl<>(List.of(a1, a2), pageable, 2);

        when(attendanceRepository.findByEmployeeId(eq(employeeId), eq(pageable))).thenReturn(page);

        PaginatedResponseDto<AttendanceResponseDto> result =
                attendanceService.getAttendancesByEmployee(employeeId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.first()).isTrue();
        assertThat(result.last()).isTrue();
    }

    @Test
    @DisplayName(
            "Should return empty paginated response with totalElements 0 and totalPages 0 when"
                    + " employee has no marks")
    void shouldReturnEmptyPaginatedResponseWhenNoMarksFound() {
        Long employeeId = 202L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Attendance> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(attendanceRepository.findByEmployeeId(eq(employeeId), eq(pageable)))
                .thenReturn(emptyPage);

        PaginatedResponseDto<AttendanceResponseDto> result =
                attendanceService.getAttendancesByEmployee(employeeId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName(
            "Should throw InvalidAttendanceException when employeeId is non-positive during"
                    + " retrieval")
    void shouldThrowWhenRetrievingWithInvalidEmployeeId() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> attendanceService.getAttendancesByEmployee(null, pageable))
                .isInstanceOf(InvalidAttendanceException.class);

        assertThatThrownBy(() -> attendanceService.getAttendancesByEmployee(0L, pageable))
                .isInstanceOf(InvalidAttendanceException.class);

        assertThatThrownBy(() -> attendanceService.getAttendancesByEmployee(-5L, pageable))
                .isInstanceOf(InvalidAttendanceException.class);

        verify(attendanceRepository, never()).findByEmployeeId(any(), any());
    }
}
