package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class CartEmptyException extends RuntimeException {
    private final Long userId;
    public CartEmptyException(Long userId) {
        super("Cart is empty");
        this.userId = userId;
    }
}
