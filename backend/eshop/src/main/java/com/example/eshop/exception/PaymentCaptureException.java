package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class PaymentCaptureException extends RuntimeException {
    private final String orderNumber;

    public PaymentCaptureException(String orderNumber, String message) {
        super("Failed to capture payment for order " + orderNumber + ": " + message);
        this.orderNumber = orderNumber;
    }
}
