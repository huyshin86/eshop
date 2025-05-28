package com.example.eshop.security.util;

import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

@Component
public class SecurityUtils {
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    private SecurityUtils() {}
    
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            logger.debug("Found user ID in security context: {}", userDetails.getId());
            return userDetails.getId();
        }
        
        logger.error("No authenticated user found in security context");
        throw new SecurityException("No authenticated user found");
    }
    
    public static boolean isResourceOwner(Long resourceId) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Checking if user {} owns resource {}", currentUserId, resourceId);
            return currentUserId.equals(resourceId);
        } catch (Exception e) {
            logger.error("Error checking resource ownership {}", e.getMessage());
            return false;
        }
    }
}
