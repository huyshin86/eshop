package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class ShippingAddressMissingException extends RuntimeException {
    private final Long userId;
    public ShippingAddressMissingException(Long userId) {
        super("Shipping address is missing");
        this.userId = userId;
    }
}
