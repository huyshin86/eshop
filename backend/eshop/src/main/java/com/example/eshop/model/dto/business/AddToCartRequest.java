package com.example.eshop.model.dto.business;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record AddToCartRequest(
        @NotNull(message = "Product is required")
        @Positive(message = "Product ID must be positive")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be at least 1")
        Integer quantity
) {
}
