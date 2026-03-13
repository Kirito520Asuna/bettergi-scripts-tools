package com.cloud_guest.aop.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.abs.AbsAuth;
import com.cloud_guest.aop.AbsToken;
import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.properties.check.TokenProperties;
import com.cloud_guest.utils.AuthContextUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author yan
 * @Date 2026/2/10 2:54:24
 * @Description
 */
@Slf4j
@Getter
@Aspect
@Component
public class TokenAspect implements AbsToken, AbsAuth {

    @Override
    public void checkToken() {
        AbsToken.super.checkToken();
        String username = AuthContextUtil.getUsernameNoThrow();
        if (StrUtil.isBlank(username)) {
            // 接收到请求，记录请求内容
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            //可能有空指针问题
            HttpServletRequest request = attributes.getRequest();
            String requestPath = request.getServletPath();
            for (String path : fetchProtectedPaths()) {
                if (fetchPathMatcher().match(path, requestPath)) {
                    ApiCode unauthorized = ApiCode.UNAUTHORIZED;
                    throw new GlobalException(unauthorized.getCode(), unauthorized.getMessage());
                }
            }
            //没登录才验证授权token
            TokenProperties tokenProperties = SpringUtil.getBean(TokenProperties.class);
            String tokenName = tokenProperties.getName();
            String tokenValue = tokenProperties.getValue();
            if (StrUtil.isNotBlank(tokenName) && StrUtil.isNotBlank(tokenValue)) {
                String token = request.getHeader(tokenName);
                log.debug("tokenName: {}, tokenValue: {}, token: {}", tokenName, tokenValue, token);
                if (!StrUtil.equals(token, tokenValue)) {
                    throw new GlobalException("token校验失败");
                }
                log().info("token校验成功");
            }
        }
    }
}
