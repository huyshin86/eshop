package com.example.eshop.security.util;

public interface CurrentUserProvider {
    Long getCurrentUserId();
    boolean isResourceOwner(Long resourceId);
}
