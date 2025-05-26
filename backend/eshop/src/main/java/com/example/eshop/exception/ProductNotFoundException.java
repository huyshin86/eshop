package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private final Long productId;
    public ProductNotFoundException(Long productId) {
        super("Product not found");
        this.productId = productId;
    }
}
