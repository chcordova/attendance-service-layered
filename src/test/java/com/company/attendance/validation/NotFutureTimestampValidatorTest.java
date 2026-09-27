package com.company.attendance.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotFutureTimestampValidatorTest {

    private NotFutureTimestampValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new NotFutureTimestampValidator();
        context = mock(ConstraintValidatorContext.class);
        builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    }

    @Test
    @DisplayName("Should return true when timestamp is null (delegates to @NotNull)")
    void shouldReturnTrueWhenNull() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    @DisplayName("Should return true when timestamp is in the past")
    void shouldReturnTrueWhenPast() {
        LocalDateTime past = LocalDateTime.now().minusHours(2);
        assertThat(validator.isValid(past, context)).isTrue();
    }

    @Test
    @DisplayName("Should return false and customize error message when timestamp is in the future")
    void shouldReturnFalseWhenFuture() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);

        boolean result = validator.isValid(future, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context)
                .buildConstraintViolationWithTemplate(
                        "Cannot register an attendance with a future timestamp: " + future);
        verify(builder).addConstraintViolation();
    }
}
