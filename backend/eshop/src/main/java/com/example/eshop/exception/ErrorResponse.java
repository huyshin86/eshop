package com.example.eshop.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Getter
public class ErrorResponse {
    private final Map<String, String> errors = new HashMap<>();
    private final String path;
    private final LocalDateTime timestamp;
    
    public void addError(String field, String message) {
        errors.put(field, message);
    }
    public ErrorResponse(String path){this.path = path; this.timestamp = LocalDateTime.now();}
}
