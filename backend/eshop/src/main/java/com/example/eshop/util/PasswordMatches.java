package com.example.eshop.util;

import com.example.eshop.security.validation.PasswordMatchesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PasswordMatches {
    String message() default "Password and confirmation password do not match.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
