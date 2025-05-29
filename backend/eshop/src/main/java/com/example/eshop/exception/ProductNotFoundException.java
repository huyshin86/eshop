package com.example.eshop.exception;

import lombok.Getter;
import java.util.List;

/**
 * Exception thrown when a product is not found.
 */
@Getter
public class ProductNotFoundException extends RuntimeException {
    
    private final List<Long> productId;
    
    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
    }
    
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.productId = null;
    }
    
    public ProductNotFoundException(List<Long> productId) {
        super("Product not found");
        this.productId = productId;
    }
}
