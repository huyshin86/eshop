package com.example.eshop.model.dto.business;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;

public record ProductRequest(

        Long id,

        @NotBlank(message = "Product name is required")
        String name,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        // Can be null not update image
        MultipartFile image,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        String categoryName,

        @NotNull
        Boolean isActive

) {}

