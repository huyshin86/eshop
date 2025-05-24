package com.example.eshop.model.dto.business;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
    Long orderId,
    String orderNumber,
    LocalDateTime orderDate,
    String orderStatus,
    BigDecimal subtotal,
    BigDecimal discountAmount,
    BigDecimal shippingCost,
    BigDecimal tax,
    BigDecimal grandTotal,
    String shippingAddress,
    List<OrderItemDto> items
) {
}
