package com.abed.perfumeshop.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TrustedEmailDomainValidator.class)
public @interface TrustedEmailDomain {

    String message() default "The email domain is not supported";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
