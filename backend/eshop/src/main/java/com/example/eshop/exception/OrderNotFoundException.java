package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class OrderNotFoundException extends RuntimeException {
    private final Long userId;
    private final Long orderId;

    public OrderNotFoundException(Long userId, Long orderId) {
        super("Order not found");
        this.userId = userId;
        this.orderId = orderId;
    }
    public OrderNotFoundException(String message){
        super(message);
        userId = null;
        orderId = null;
    }
}
