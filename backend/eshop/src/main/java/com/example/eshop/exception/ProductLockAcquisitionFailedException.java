package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class ProductLockAcquisitionFailedException extends RuntimeException {
    private final Long userId;
    public ProductLockAcquisitionFailedException(Long userId) {
        super("Too Many Requests. Please try again.");
        this.userId = userId;
    }
}
