package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class InsufficientProductStockException extends RuntimeException {
    private final Long productId;
    private final Integer stock;

    public InsufficientProductStockException(Long productId, Integer stock) {
        super("Insufficient stock. Available: " + stock);
        this.productId = productId;
        this.stock = stock;
    }
}
