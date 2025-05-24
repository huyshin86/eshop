package com.example.eshop.model.dto.business;

import java.math.BigDecimal;

public record OrderItemDto(
        Long orderItemId,
        ProductDto product,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal total
) {
}
