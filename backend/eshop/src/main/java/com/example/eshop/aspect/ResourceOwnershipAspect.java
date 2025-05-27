package com.example.eshop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.example.eshop.exception.ResourceOwnershipException;
import com.example.eshop.util.CheckResourceOwnership;
import com.example.eshop.security.util.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class ResourceOwnershipAspect {
    private static final Logger logger = LoggerFactory.getLogger(ResourceOwnershipAspect.class);

    @Before("@annotation(checkOwnership)")
    public void checkOwnership(JoinPoint joinPoint, CheckResourceOwnership checkOwnership) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long resourceId) {
            logger.debug("Checking ownership for resource ID: {}", resourceId);
            try {
                Long currentUserId = SecurityUtils.getCurrentUserId();
                logger.debug("Current user ID from security context: {}", currentUserId);
                
                if (!currentUserId.equals(resourceId)) {
                    logger.debug("Access denied: User {} attempting to access resource {}", 
                               currentUserId, resourceId);
                    throw new ResourceOwnershipException(
                        "You don't have permission to access this resource", 
                        resourceId, 
                        currentUserId
                    );
                }
            } catch (ResourceOwnershipException e) {
                logger.error("Resource ownership check failed: {}", e.getMessage());
                throw e; // Rethrow the exception to be handled by the global exception handler
            } catch (Exception e) {
                logger.error("An unexpected error occurred: {}", e.getMessage());
                throw new ResourceOwnershipException(
                    "An unexpected error occurred while checking resource ownership", 
                    resourceId, 
                    null
                );
            }
        }
    }
    
    private Long extractOwnerId(Object[] args) {
        return null;
    }
}
