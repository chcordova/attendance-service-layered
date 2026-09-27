package com.company.attendance.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PageSizeValidatorTest {

    private PageSizeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PageSizeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    @DisplayName("Should return true when page size is null")
    void shouldReturnTrueWhenNull() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 50, 100})
    @DisplayName("Should return true when page size is between 1 and 100")
    void shouldReturnTrueWhenWithinRange(Integer size) {
        assertThat(validator.isValid(size, context)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -50, 101, 200})
    @DisplayName("Should return false when page size is out of range (< 1 or > 100)")
    void shouldReturnFalseWhenOutOfRange(Integer size) {
        assertThat(validator.isValid(size, context)).isFalse();
    }
}
