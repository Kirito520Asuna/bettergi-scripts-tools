package com.cloud_guest.redis.aop.aspect;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.redis.abs.aop.AbsRedisAspect;
import com.cloud_guest.redis.abs.ban.BanManager;
import com.cloud_guest.redis.aop.ban.Ban;
import com.cloud_guest.redis.ban.BanConfiguration;
import com.cloud_guest.redis.ban.BanType;
import com.cloud_guest.redis.ban.SimpleBanManager;
import com.cloud_guest.redis.config.RedissonConfig;
import com.cloud_guest.redis.exception.BanException;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

import static cn.hutool.extra.servlet.ServletUtil.getClientIP;


/**
 * @Author yan
 * @Date 2025/5/20 14:49:21
 * @Description
 */
@Aspect
@Slf4j
@Component
@ConditionalOnBean(RedissonConfig.class)
public class RedisBanAspect implements AbsRedisAspect {
    static BanManager DEFAULT_BAN_MANAGER;
    static BanConfiguration DEFAULT_BAN_CONFIGURATION;

    static {
        try {
            DEFAULT_BAN_MANAGER = SpringUtil.getBean(BanManager.class);
        } catch (NoSuchBeanDefinitionException e) {
            // 如果没有配置 BanManager，则新建一个
            log.warn(e.getMessage());
            DEFAULT_BAN_MANAGER = new SimpleBanManager();
        }

        try {
            DEFAULT_BAN_CONFIGURATION = SpringUtil.getBean(BanConfiguration.class);
        } catch (NoSuchBeanDefinitionException e) {
            // 如果没有配置 BanConfiguration，则新建一个
            log.warn(e.getMessage());
            DEFAULT_BAN_CONFIGURATION = new BanConfiguration();
        }
    }


    // 全局检查：白名单、黑名单和全局 IP 封禁
    //@Around(value = "@annotation(org.springframework.web.bind.annotation.RestController)")
    //@Around("execution(* com..*Controller.*(..))")
    @Around("execution(* com..*Controller.*(..))")
    public Object checkGlobalBan(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("[{}]执行全局检查", System.currentTimeMillis());
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.debug("内部自调 没有请求上下文，放行");
            //内部自调 如果没有请求上下文，则直接放行
            return joinPoint.proceed();
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String ipAddress = getClientIP(request);
        BanConfiguration banConfiguration = DEFAULT_BAN_CONFIGURATION;
        boolean globalBanEnabled = banConfiguration.isGlobalBanEnabled();
        Set<String> whitelist = banConfiguration.getWhitelist();
        Set<String> blacklist = banConfiguration.getBlacklist();

        // 检查白名单
        if (whitelist.contains(ipAddress)) {
            log.debug("IP 地址 {} 在白名单中，放行", ipAddress);
            return joinPoint.proceed(); // 白名单直接放行
        }

        // 检查黑名单
        if (blacklist.contains(ipAddress)) {
            //throw new BanException("IP 地址 " + ipAddress + " 在黑名单中");
            log.debug("IP 地址 {} 在黑名单中，封禁", ipAddress);
            throw new BanException();
        }

        BanManager banManager = DEFAULT_BAN_MANAGER;

        // 检查全局 IP 封禁
        if (globalBanEnabled && banManager.isIPBanned(ipAddress)) {
            //throw new BanException("IP 地址 " + ipAddress + " 已被全局封禁");
            throw new BanException();
        }

        return around(joinPoint);
    }

    @Override
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ipAddress = getClientIP(request);
        String interfaceName = joinPoint.getSignature().toString();
        // 获取注解参数
        Ban ban = getAnnotation(joinPoint,Ban.class);
        if (ban == null) {
            return joinPoint.proceed();
        }
        log.info("[{}]执行检查", System.currentTimeMillis());
        BanType banType = ban.type();
        long windowSeconds = ban.windowSeconds();
        long maxRequests = ban.maxRequests();
        long banDuration = ban.banDuration();
        java.util.concurrent.TimeUnit timeUnit = ban.timeUnit();

        BanManager banManager = DEFAULT_BAN_MANAGER;

        // 检查请求频率并自动封禁
        String rateKey;
        switch (banType) {
            case IP:
                rateKey = ipAddress;
                if (banManager.checkAndBan(rateKey, windowSeconds, maxRequests, banDuration, timeUnit, banType, ipAddress, interfaceName)
                        || banManager.isIPBanned(ipAddress)) {
                    //throw new BanException("IP 地址 " + ipAddress + " 已被封禁");
                    throw new BanException();
                }
                break;
            case INTERFACE:
                rateKey = interfaceName;
                if (banManager.checkAndBan(rateKey, windowSeconds, maxRequests, banDuration, timeUnit, banType, ipAddress, interfaceName)
                        || banManager.isInterfaceBanned(interfaceName)) {
                    //throw new BanException("接口 " + interfaceName + " 已被封禁");
                    throw new BanException();
                }
                break;
            case IP_INTERFACE:
                rateKey = ipAddress + "&" + interfaceName;
                if (banManager.checkAndBan(rateKey, windowSeconds, maxRequests, banDuration, timeUnit, banType, ipAddress, interfaceName)
                        || banManager.isIPInterfaceBanned(ipAddress, interfaceName)) {
                    //throw new BanException("IP 地址 " + ipAddress + " 在接口 " + interfaceName + " 上被封禁");
                    throw new BanException();
                }
                break;
        }

        return AbsRedisAspect.super.around(joinPoint);
    }
}
