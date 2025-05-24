package com.example.eshop.model.dto.auth.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record RegisterDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Password fields are required")
        @Valid
        PasswordFields passwordFields,

        @NotBlank(message = "First name is required")
        @Size(min = 2, message = "First name must be at least 2 characters long")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, message = "Last name must be at least 2 characters long")
        String lastName,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "\\d{10,15}", message = "Phone number must be between 10 and 15 digits")
        String phone) {
}
