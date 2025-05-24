package com.example.eshop.model.common;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ADMIN"),
    CUSTOMER("CUSTOMER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String toStringRole(){
        return "ROLE_" + value;
    }
}
