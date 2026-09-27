package com.company.attendance.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Custom constraint annotation to validate pagination page index. */
@Documented
@Constraint(validatedBy = PageNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPageNumber {
    String message() default "Page index must not be less than zero.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
