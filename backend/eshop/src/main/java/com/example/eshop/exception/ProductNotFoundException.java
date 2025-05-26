package com.example.eshop.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private final List<Long> productId;

    public ProductNotFoundException(List<Long> productId) {
        super("Product not found");
        this.productId = productId;
    }
}
