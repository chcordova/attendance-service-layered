package com.company.attendance.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidEmployeeId}. Checks that the employee ID is non-null and
 * greater than zero.
 */
public class EmployeeIdValidator implements ConstraintValidator<ValidEmployeeId, Long> {

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value > 0;
    }
}
