package com.cloud_guest.aop;

import com.cloud_guest.aop.security.Login;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @Author yan
 * @Date 2024/9/27 上午10:49:44
 * @Description
 */

public interface AbsLogin extends AbsAop {

    @Override
    default int getOrder() {
        return AopConstants.LoginOrder;
    }

    @Override
    @Pointcut(value = "@annotation(com.cloud_guest.aop.security.Login)")
    default void Aop() {
    }

    @Override
    @Around(value = "Aop()")
    default Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        log();
        // 注解鉴权
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        checkMethodAnnotation(signature.getMethod());
        try {
            // 执行原有逻辑
            return AbsAop.super.around(joinPoint);
        } catch (Throwable e) {
            throw e;
        }
    }

    /**
     * 对一个Method对象进行注解检查
     */
    default void checkMethodAnnotation(Method method) {
        // 校验 @RequiresLogin 注解
        Login login = method.getAnnotation(Login.class);
        if (login != null) {
            checkLogin();
        }
    }

    /**
     * 检查登陆
     */
    default void checkLogin() {
    }
}
