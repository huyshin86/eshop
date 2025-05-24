package com.example.eshop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResourceOwnershipException extends RuntimeException {
    private final Long resourceId;
    private final Long userId;

    public ResourceOwnershipException(String message, Long resourceId, Long userId) {
        super(message);
        this.resourceId = resourceId;
        this.userId = userId;
    }

}
