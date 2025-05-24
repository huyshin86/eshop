package com.example.eshop.model.dto.business;

public record CartItemDto(
        Long cartItemId,
        ProductDto product,
        Integer quantity,
        Boolean isAvailableInStock
) {
}
