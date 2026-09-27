package com.company.attendance.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmployeeIdValidatorTest {

    private EmployeeIdValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new EmployeeIdValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    @DisplayName("Should return false when employeeId is null")
    void shouldReturnFalseWhenNull() {
        assertThat(validator.isValid(null, context)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 42L, 999999L})
    @DisplayName("Should return true when employeeId is strictly positive")
    void shouldReturnTrueWhenPositive(Long employeeId) {
        assertThat(validator.isValid(employeeId, context)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -50L})
    @DisplayName("Should return false when employeeId is zero or negative")
    void shouldReturnFalseWhenZeroOrNegative(Long employeeId) {
        assertThat(validator.isValid(employeeId, context)).isFalse();
    }
}
