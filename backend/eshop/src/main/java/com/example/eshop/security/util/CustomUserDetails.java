package com.example.eshop.security.util;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    private final Long id;

    public CustomUserDetails(String username, String password, 
            Collection<? extends GrantedAuthority> authorities,
            Long id, boolean enabled, boolean accountNonLocked) {
        super(username, password, enabled, true, true, accountNonLocked, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
