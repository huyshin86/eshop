package com.example.eshop.model.dto.business.response;

import java.math.BigDecimal;
public record OrderItemResponseDto(
    Long orderItemId,
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal total
) {}
