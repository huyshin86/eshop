package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final Long id;
    public UserNotFoundException(Long id) {
        super("User not found");
        this.id = id;
    }
}
