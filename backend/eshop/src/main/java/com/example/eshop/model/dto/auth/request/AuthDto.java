package com.example.eshop.model.dto.auth.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email, 

        @NotBlank(message = "Password is required")
        // @Size(min = 8, message = "Password must be at least 8 characters long") Since test password is short
        String password, 

        String confirmCode) {
}
