package com.cloud_guest.redis.aop.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.redis.abs.aop.AbsRedisAspect;
import com.cloud_guest.redis.aop.redis.RedisCacheEvict;
import com.cloud_guest.redis.config.RedissonConfig;
import com.cloud_guest.redis.service.RedisService;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author yan
 * @Date 2024/7/26 0026 15:36:50
 * @Description
 */

@Aspect
@Slf4j
@Component
@ConditionalOnBean(RedissonConfig.class)
@Getter
public class RedisCacheEvictAspect implements AbsRedisAspect {
    @Lazy
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 缓存参数缓存key模板
     */
    protected ThreadLocal<RedisCacheParameters> redisCacheThreadLocal = new ThreadLocal<>();

    public RedisCacheParameters getOne() {
        RedisCacheParameters one = redisCacheThreadLocal.get();
        if (one == null) {
            one = new RedisCacheParameters();
        }
        setOne(one);
        return one;
    }

    public RedisCacheParameters setOne(RedisCacheParameters one) {
        redisCacheThreadLocal.set(one);
        return one;
    }

    @Override
    @Pointcut("@annotation(com.cloud_guest.redis.aop.redis.RedisCacheEvict)")
    public void pointcutAspect() {
    }

    @SneakyThrows
    @Override
    @AfterReturning(pointcut = "pointcutAspect()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            RedisCacheEvict cacheEvict = getAnnotation(joinPoint, RedisCacheEvict.class);
            if (cacheEvict == null) {
                return;
            }
            setOne(setRequesRedisCacheParameters(getOne(), joinPoint));
            String cacheName = cacheEvict.cacheName();
            String key = cacheEvict.key();
            String condition = cacheEvict.condition();
            String requestAsName = cacheEvict.requestAsName();
            String responseAsName = cacheEvict.responseAsName();

            String json;
            try {
                json = SpringUtil.getBean(ObjectMapper.class).writeValueAsString(result);
            } catch (JsonMappingException e) {
                json = JSONUtil.toJsonStr(result);
            }
            if (!JSONUtil.isTypeJSON(json)) {
                json = JSONUtil.toJsonStr(result);
            }
            setOne(getOne().setResponse(JSONUtil.toBean(json, Map.class)));
            //setOne(getOne().setResponse(JSONUtil.toBean(SpringUtil.getBean(ObjectMapper.class).writeValueAsString(result),Map.class)));
            RedisCacheParameters one = getOne();
            Map<String, Object> request = one.getRequest();
            Map<String, Object> response = one.getResponse();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put(requestAsName, request);
            map.put(responseAsName, response);
            JSONObject jsonObject = new JSONObject(map);

            cacheName = effectiveSplicingString(cacheName, jsonObject, CollUtil.newArrayList(splicer), OperationType.str);
            key = effectiveSplicingString(key, jsonObject, CollUtil.newArrayList(splicer), OperationType.str);

            String formatKey = String.format(templateKey, cacheName, key);

            // 判断是否需要缓存
            try {
                condition = effectiveSplicingString(condition, jsonObject, comparisonOperators, OperationType.condition);
            } catch (Exception e) {
                log.error("condition is error,condition:{}", condition);
                condition = cacheEvict.condition();
            }

            boolean okCondition = verifiedOkCondition(condition);
            // 判断条件 判断是否需要缓存
            if (okCondition) {
                //if (cacheEvict.isHash()) {
                //    redisTemplate.opsForHash().delete(cacheName, key);
                //    log.info("delete redis hash key:{},hashKey:{}", cacheName, key);
                //}else {
                //    redisTemplate.delete(formatKey);
                //    log.info("delete redis key:{}", formatKey);
                //}
                log.debug("@RedisCacheEvict");
                if (cacheEvict.isHash()) {
                    formatKey = key;
                }
                SpringUtil.getBean(RedisService.class)
                        .del(cacheName, cacheEvict.isHash(), formatKey);
            }
        } finally {
            setOne(null);
        }

    }
}
