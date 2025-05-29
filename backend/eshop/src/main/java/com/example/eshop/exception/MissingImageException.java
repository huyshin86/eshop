package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class MissingImageException extends RuntimeException {
    private final String productName;
    public MissingImageException(String productName) {
        super("Missing image for product" + productName);
        this.productName = productName;
    }
}
