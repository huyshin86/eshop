package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class CartItemNotFoundException extends RuntimeException {
    private final Long userId;
    private final Long productId;

    public CartItemNotFoundException(Long userId, Long productId) {
        super("Cart item not found");
        this.userId = userId;
        this.productId = productId;
    }
}
