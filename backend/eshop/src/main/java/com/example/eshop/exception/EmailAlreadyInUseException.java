package com.example.eshop.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyInUseException extends RuntimeException {
    private final String email;

    public EmailAlreadyInUseException(String email) {
        super("Email is already in use.");
        this.email = email;
    }
}
