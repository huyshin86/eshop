package com.example.eshop.model.dto.business;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        Integer stockQuantity,
        Long categoryId,
        String categoryName,
        Boolean isActive
) {
}
