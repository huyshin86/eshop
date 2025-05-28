package com.example.eshop.model.dto.business;

public record PayPalOrderDetailDto(
        String paypalOrderId,
        String approvalUrl
) {
}
