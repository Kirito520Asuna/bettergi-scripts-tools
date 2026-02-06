package com.cloud_guest.redis.aop.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import com.cloud_guest.redis.abs.aop.AbsRedisAspect;
import com.cloud_guest.redis.aop.redis.RedisLock;
import com.cloud_guest.redis.config.RedissonConfig;
import com.cloud_guest.redis.exception.RedisException;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author yan
 * @Date 2024/5/29 0029 16:48
 * @Description
 */
@Aspect
@Slf4j
@Component
@ConditionalOnBean(RedissonConfig.class)
@Getter
public class RedisLockAspect implements AbsRedisAspect {
    /**
     * 缓存参数缓存key模板
     */
    protected ThreadLocal<AbsRedisAspect.RedisEntity<RLock>> redisLockCacheThreadLocal = new ThreadLocal<>();

    public RedisEntity<RLock> getOne() {
        RedisEntity<RLock> one = redisLockCacheThreadLocal.get();
        if (one == null) {
            one = new RedisEntity().setRedisCacheParameters(new RedisCacheParameters());
        }
        setOne(one);
        return one;
    }

    public RedisEntity<RLock> setOne(RedisEntity<RLock> one) {
        redisLockCacheThreadLocal.set(one);
        return one;
    }

    @Resource
    @Lazy
    private RedissonClient redissonClient;

    protected RedisLock getAnnotation(JoinPoint joinPoint) {
        return getAnnotation(joinPoint, RedisLock.class);
    }

    @Override
    @Pointcut("@annotation(com.cloud_guest.redis.aop.redis.RedisLock)")
    public void pointcutAspect() {
        AbsRedisAspect.super.pointcutAspect();
    }

    @Override
    @Around(value = "pointcutAspect()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //joinPoint.proceed() 目标方法执行
        RedisLock annotation = getAnnotation(joinPoint);
        RedisEntity<RLock> one = getOne();
        try {
            if (annotation == null) {
                return AbsRedisAspect.super.around(joinPoint);
            }
            one = one.setRedisCacheParameters(setRequesRedisCacheParameters(one.getRedisCacheParameters(), joinPoint));
        } finally {
            setOne(one);
        }

        RedisCacheParameters parameters = one.getRedisCacheParameters();
        Map<String, Object> request = parameters.getRequest();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(annotation.requestAsName(), request);
        JSONObject jsonObject = new JSONObject(map);

        String key = effectiveSplicingString(annotation.key(), jsonObject, CollUtil.newArrayList(splicer), OperationType.str);
        log.debug("@RedisLock");
        RLock lock = redissonClient.getLock(key);
        boolean isLocked = lock.isLocked();
        try {
            log.info("获取分布式锁==>key:{}",key);
            isLocked = lock.tryLock(annotation.waitTime(), annotation.leaseTime(), annotation.timeUnit());
            if (!isLocked) {
                throw new RedisException(annotation.exceptionMessage());
            }
            return AbsRedisAspect.super.around(joinPoint);
        } finally {
            //确保只有当前线程持有锁时，才解锁
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
