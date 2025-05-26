package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class CheckoutFailException extends RuntimeException {
    private final Long userId;
    private final String exceptionMessage;
    public CheckoutFailException(Long userId, String exceptionMessage) {
        super("Checkout failed after multiple attempts. Please try again later.");
        this.userId = userId;
        this.exceptionMessage = exceptionMessage;
    }
}
