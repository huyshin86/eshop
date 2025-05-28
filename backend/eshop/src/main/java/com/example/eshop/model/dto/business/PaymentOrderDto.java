package com.example.eshop.model.dto.business;

import java.math.BigDecimal;

public record PaymentOrderDto(
        Long businessOrderId,
        String businessOrderNumber,
        PayPalOrderDetailDto payPalOrderDetailDto,
        BigDecimal totalAmount,
        String status
) {
}
