package com.example.eshop.model.dto.business;

import java.math.BigDecimal;

public record ProductDto(
        Long productId,
        String productName,
        String description,
        BigDecimal price,
        String imageUrl,
        Integer stock,
        Boolean isActive
) {
}
