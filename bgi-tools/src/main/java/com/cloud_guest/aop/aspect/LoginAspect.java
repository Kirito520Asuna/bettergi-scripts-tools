package com.cloud_guest.aop.aspect;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.aop.AbsLogin;
import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.properties.auth.AuthProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/10 14:02:21
 * @Description
 */
@Slf4j
@Getter
@Aspect
@Component
public class LoginAspect implements AbsLogin {
    @Override
    public void checkLogin() {
        AbsLogin.super.checkLogin();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AuthProperties authProperties = SpringUtil.getBean(AuthProperties.class);
        List<String> collect = authProperties.getUsers().stream().map(user -> user.getUsername()).collect(Collectors.toList());
        if (!collect.contains(username)) {
            ApiCode fail = ApiCode.UNAUTHORIZED;
            throw new GlobalException(fail.getCode(),fail.getMessage());
        }
    }
}
