package com.example.eshop.model.dto.business;

import java.math.BigDecimal;
import java.util.List;

public record UserCartDto(
        List<CartItemDto> items,
        BigDecimal totalPrice
) {
}
