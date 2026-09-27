package com.company.attendance.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom constraint annotation to validate employee identifiers. Enforces that the identifier is
 * non-null and strictly positive.
 */
@Documented
@Constraint(validatedBy = EmployeeIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmployeeId {
    String message() default "Employee ID must be a positive number.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
