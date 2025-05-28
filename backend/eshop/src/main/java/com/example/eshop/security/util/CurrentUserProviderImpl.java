package com.example.eshop.security.util;

import org.springframework.stereotype.Component;

@Component
// For testing injection purpose
public class CurrentUserProviderImpl implements CurrentUserProvider {
    @Override
    public Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    @Override
    public boolean isResourceOwner(Long resourceId) {
        return SecurityUtils.isResourceOwner(resourceId);
    }
}
