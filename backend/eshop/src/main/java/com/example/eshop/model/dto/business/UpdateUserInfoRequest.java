package com.example.eshop.model.dto.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserInfoRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, message = "First name must be at least 2 characters long")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, message = "Last name must be at least 2 characters long")
        String lastName,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "\\d{10,15}", message = "Phone number must be between 10 and 15 digits")
        String phoneNumber,

        @NotBlank(message = "Address is required")
        String address
) {
}
