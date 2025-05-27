package com.example.eshop.util;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LockTimeout {
    int seconds() default 10; // default to 10 seconds
}
