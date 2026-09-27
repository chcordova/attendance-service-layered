package com.company.attendance.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

/**
 * Validator implementation for {@link NotFutureTimestamp}. Verifies that the provided {@link
 * LocalDateTime} is not after the current system time and builds dynamic error diagnostics.
 */
public class NotFutureTimestampValidator
        implements ConstraintValidator<NotFutureTimestamp, LocalDateTime> {

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Delegation to @NotNull
        }
        if (value.isAfter(LocalDateTime.now())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Cannot register an attendance with a future timestamp: " + value)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
