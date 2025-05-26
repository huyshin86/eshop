package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class ProductNotAvailableException extends RuntimeException {
    private final Long productId;

    public ProductNotAvailableException(Long productId) {
        super("Product is not available");
        this.productId = productId;
    }
}
