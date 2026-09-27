package com.company.attendance.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidPageSize}. Checks that the page size is strictly greater
 * than zero (and optionally <= 100).
 */
public class PageSizeValidator implements ConstraintValidator<ValidPageSize, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value >= 1 && value <= 100;
    }
}
