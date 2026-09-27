package com.company.attendance.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PageNumberValidatorTest {

    private PageNumberValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PageNumberValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    @DisplayName("Should return true when page number is null")
    void shouldReturnTrueWhenNull() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 100})
    @DisplayName("Should return true when page number is zero or positive")
    void shouldReturnTrueWhenZeroOrPositive(Integer page) {
        assertThat(validator.isValid(page, context)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -99})
    @DisplayName("Should return false when page number is negative")
    void shouldReturnFalseWhenNegative(Integer page) {
        assertThat(validator.isValid(page, context)).isFalse();
    }
}
