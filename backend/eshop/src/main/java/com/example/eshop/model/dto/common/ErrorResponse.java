package com.example.eshop.model.dto.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String path;
    private final Map<String, String> errors = new HashMap<>();

    
    public void addError(String field, String message) {
        errors.put(field, message);
    }
    public ErrorResponse(HttpStatus httpStatus, String path){
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.status = httpStatus.value();
    }
}
