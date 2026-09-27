package com.company.attendance.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidPageNumber}. Checks that the page index is non-negative.
 */
public class PageNumberValidator implements ConstraintValidator<ValidPageNumber, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value >= 0;
    }
}
