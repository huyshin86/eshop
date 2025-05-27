package com.example.eshop.aspect;

import com.example.eshop.util.LockTimeout;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LockTimeoutAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Pointcut("@annotation(lockTimeout)")
    public void withLockTimeout(LockTimeout lockTimeout) {}

    @Before("withLockTimeout(lockTimeout)")
    public void setLockTimeout(JoinPoint joinPoint, LockTimeout lockTimeout) {
        int timeout = lockTimeout.seconds();
        log.debug("Setting innodb_lock_wait_timeout to {} seconds", timeout);
        entityManager.createNativeQuery("SET innodb_lock_wait_timeout = " + timeout).executeUpdate();
    }
}
