package com.company.attendance.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.attendance.exception.FutureDateException;
import com.company.attendance.exception.InvalidAttendanceException;
import com.company.attendance.model.AttendanceType;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class DtoAndExceptionTest {

    @Test
    @DisplayName("Should test FutureDateException constructors")
    void shouldTestFutureDateExceptionConstructors() {
        FutureDateException exDefault = new FutureDateException();
        assertThat(exDefault.getMessage())
                .isEqualTo("Cannot register an attendance with a future timestamp.");

        LocalDateTime futureTime = LocalDateTime.of(2030, 1, 1, 12, 0);
        FutureDateException exTimestamp = new FutureDateException(futureTime);
        assertThat(exTimestamp.getMessage())
                .isEqualTo("Cannot register an attendance with a future timestamp: " + futureTime);

        FutureDateException exCustom = new FutureDateException("Custom future message");
        assertThat(exCustom.getMessage()).isEqualTo("Custom future message");
    }

    @Test
    @DisplayName("Should test InvalidAttendanceException constructors")
    void shouldTestInvalidAttendanceExceptionConstructors() {
        InvalidAttendanceException exDefault = new InvalidAttendanceException();
        assertThat(exDefault.getMessage()).isEqualTo("Invalid attendance data.");

        InvalidAttendanceException exCustom =
                new InvalidAttendanceException("Custom invalid message");
        assertThat(exCustom.getMessage()).isEqualTo("Custom invalid message");
    }

    @Test
    @DisplayName("Should test PaginatedResponseDto edge cases and immutability")
    void shouldTestPaginatedResponseDto() {
        PaginatedResponseDto<String> nullContentDto =
                new PaginatedResponseDto<>(null, 0, 10, 0L, 0, true, true);
        assertThat(nullContentDto.content()).isEmpty();

        PaginatedResponseDto<String> fromNullPage = PaginatedResponseDto.of(null);
        assertThat(fromNullPage.content()).isEmpty();
        assertThat(fromNullPage.totalElements()).isEqualTo(0L);
        assertThat(fromNullPage.totalPages()).isEqualTo(0);

        PageImpl<String> emptyPage =
                new PageImpl<>(Collections.emptyList(), PageRequest.of(2, 5), 0);
        PaginatedResponseDto<String> fromEmptyPage = PaginatedResponseDto.of(emptyPage);
        assertThat(fromEmptyPage.page()).isEqualTo(2);
        assertThat(fromEmptyPage.size()).isEqualTo(5);
        assertThat(fromEmptyPage.totalElements()).isEqualTo(0L);
        assertThat(fromEmptyPage.totalPages()).isEqualTo(0);
        assertThat(fromEmptyPage.first()).isTrue();
        assertThat(fromEmptyPage.last()).isTrue();

        PageImpl<String> nonEmptyPage = new PageImpl<>(List.of("A", "B"), PageRequest.of(0, 2), 2);
        PaginatedResponseDto<String> fromNonEmpty = PaginatedResponseDto.of(nonEmptyPage);
        assertThat(fromNonEmpty.content()).containsExactly("A", "B");
        assertThat(fromNonEmpty.totalPages()).isEqualTo(1);
        assertThat(fromNonEmpty.totalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should test ErrorResponseDto constructors")
    void shouldTestErrorResponseDtoConstructors() {
        ErrorResponseDto dtoSimple =
                new ErrorResponseDto(400, "Bad Request", "INVALID_REQUEST", "Invalid", "/api/test");
        assertThat(dtoSimple.timestamp()).isNotNull();
        assertThat(dtoSimple.status()).isEqualTo(400);
        assertThat(dtoSimple.details()).isNull();

        Map<String, String> details = Map.of("field", "error");
        ErrorResponseDto dtoDetailed =
                new ErrorResponseDto(
                        400,
                        "Bad Request",
                        "FIELD_VALIDATION_FAILED",
                        "Failed",
                        "/api/test",
                        details);
        assertThat(dtoDetailed.details()).isEqualTo(details);
    }

    @Test
    @DisplayName("Should test AttendanceRequestDto Lombok features")
    void shouldTestAttendanceRequestDto() {
        LocalDateTime now = LocalDateTime.now();
        AttendanceRequestDto dto1 = new AttendanceRequestDto(100L, now, AttendanceType.CHECK_IN);
        AttendanceRequestDto dto2 =
                AttendanceRequestDto.builder()
                        .employeeId(100L)
                        .timestamp(now)
                        .type(AttendanceType.CHECK_IN)
                        .build();

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).contains("employeeId=100");

        AttendanceRequestDto emptyDto = new AttendanceRequestDto();
        emptyDto.setEmployeeId(200L);
        emptyDto.setTimestamp(now);
        emptyDto.setType(AttendanceType.CHECK_OUT);

        assertThat(emptyDto.getEmployeeId()).isEqualTo(200L);
        assertThat(emptyDto.getTimestamp()).isEqualTo(now);
        assertThat(emptyDto.getType()).isEqualTo(AttendanceType.CHECK_OUT);
    }

    @Test
    @DisplayName("Should test AttendanceResponseDto Record features")
    void shouldTestAttendanceResponseDto() {
        LocalDateTime now = LocalDateTime.now();
        AttendanceResponseDto response1 =
                new AttendanceResponseDto(1L, 100L, now, AttendanceType.CHECK_IN);
        AttendanceResponseDto response2 =
                new AttendanceResponseDto(1L, 100L, now, AttendanceType.CHECK_IN);

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1.id()).isEqualTo(1L);
        assertThat(response1.employeeId()).isEqualTo(100L);
        assertThat(response1.timestamp()).isEqualTo(now);
        assertThat(response1.type()).isEqualTo(AttendanceType.CHECK_IN);
        assertThat(response1.toString()).contains("id=1");
    }
}
