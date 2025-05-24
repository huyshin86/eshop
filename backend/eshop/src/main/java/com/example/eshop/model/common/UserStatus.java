package com.example.eshop.model.common;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    LOCKED("LOCKED");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }
}
