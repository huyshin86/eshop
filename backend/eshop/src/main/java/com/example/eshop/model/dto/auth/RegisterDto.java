package com.example.eshop.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*+=]).*$", 
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character from @#$%^&*+=")
        String password,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword,

        @Size(min = 2, message = "First name must be at least 2 characters long")
        String firstName,

        @Size(min = 2, message = "Last name must be at least 2 characters long")
        String lastName,

        @Pattern(regexp = "\\d{10,15}", message = "Phone number must be between 10 and 15 digits")
        String phone) {
}
