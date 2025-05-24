package com.example.eshop.model.dto.auth.request;

import com.example.eshop.security.validation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordMatches
public record PasswordFields(
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*+=]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character from @#$%^&*+=")
        String password,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword) {
}