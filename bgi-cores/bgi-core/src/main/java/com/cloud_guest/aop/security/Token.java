package com.cloud_guest.aop.security;

import java.lang.annotation.*;

/**
 * @Author yan
 * @Date 2026/2/10 2:53:01
 * @Description
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Token {
}
