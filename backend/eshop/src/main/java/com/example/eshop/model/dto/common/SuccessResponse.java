package com.example.eshop.model.dto.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class SuccessResponse<T> {
    private final LocalDateTime timestamp;
    private final int status;
    private final String message;
    private final T data;

    private SuccessResponse(HttpStatus httpStatus, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.status = httpStatus.value();
        this.message = message;
        this.data = data;
    }

    // For message response only
    public SuccessResponse(HttpStatus httpStatus, String message) {
        this(httpStatus, message, null);
    }

    // For 200 OK response with data
    public SuccessResponse(T data) {
        this(HttpStatus.OK, "Operation successful", data);
    }
}
