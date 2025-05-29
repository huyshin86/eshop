package com.example.eshop.exception;

public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException(String message) {
        super(message);
    }
    
    public DuplicateCategoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
