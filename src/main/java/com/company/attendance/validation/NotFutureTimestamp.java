package com.company.attendance.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom constraint annotation to validate that an attendance timestamp does not occur in the
 * future.
 */
@Documented
@Constraint(validatedBy = NotFutureTimestampValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotFutureTimestamp {
    String message() default "Cannot register an attendance with a future timestamp.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
