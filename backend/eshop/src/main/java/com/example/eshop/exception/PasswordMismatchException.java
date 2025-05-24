package com.example.eshop.exception;

// For update password (old != new)
public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("New password does not match old password");
    }
}
