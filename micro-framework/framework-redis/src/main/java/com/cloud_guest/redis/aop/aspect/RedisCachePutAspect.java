package com.cloud_guest.redis.aop.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.redis.abs.aop.AbsRedisAspect;
import com.cloud_guest.redis.aop.redis.RedisCachePut;
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
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2024/5/24 0024 10:54
 * @Description
 */
@Aspect
@Slf4j
@Component
@ConditionalOnBean(RedissonConfig.class)
@Getter
public class RedisCachePutAspect implements AbsRedisAspect {
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

    public RedisCacheParameters setOne(AbsRedisAspect.RedisCacheParameters one) {
        redisCacheThreadLocal.set(one);
        return one;
    }

    @Override
    @Pointcut("@annotation(com.cloud_guest.redis.aop.redis.RedisCachePut)")
    public void pointcutAspect() {
    }

    @Override
    @SneakyThrows
    @Before(value = "pointcutAspect()")
    public void doBefore(JoinPoint joinPoint) {
        RedisCachePut cachePut = getAnnotation(joinPoint, RedisCachePut.class);
        if (cachePut == null) {
            return;
        }
        setOne(setRequesRedisCacheParameters(getOne(), joinPoint));
    }


    @SneakyThrows
    @Override
    @AfterReturning(pointcut = "pointcutAspect()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            RedisCachePut cachePut = getAnnotation(joinPoint, RedisCachePut.class);
            if (cachePut == null) {
                return;
            }
            String cacheName = cachePut.cacheName();
            String key = cachePut.key();
            TimeUnit timeUnit = cachePut.timeUnit();
            long timout = cachePut.timeout();
            String condition = cachePut.condition();
            String value = cachePut.value();
            String requestAsName = cachePut.requestAsName();
            String responseAsName = cachePut.responseAsName();
            boolean openReplace = cachePut.openReplace();
            String[] replaceSplicingList = cachePut.replaceSplicingList();
            boolean random = cachePut.random();
            String randomRange = cachePut.randomRange();

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
            if (StrUtil.isNotBlank(value)) {
                value = effectiveSplicingString(value, jsonObject, CollUtil.newArrayList(splicer), OperationType.str);
            }

            String formatKey = String.format(templateKey, cacheName, key);
            if (openReplace) {
                if (replaceSplicingList.length != 2) {
                    replaceSplicingList = new String[]{":", ":"};
                }
                formatKey = replaceKey(formatKey, replaceSplicingList);
            }
            // 判断是否需要缓存
            try {
                condition = effectiveSplicingString(condition, jsonObject, comparisonOperators, OperationType.condition);
            } catch (Exception e) {
                log.error("condition is error,condition:{}", condition);
                condition = cachePut.condition();
            }

            boolean okCondition = verifiedOkCondition(condition);
            // 判断条件 判断是否需要缓存
            if (okCondition) {
                Object setValue = result;
                if (StrUtil.isNotBlank(value)) {
                    setValue = value;
                }
                log.debug("@RedisCachePut");
                if (cachePut.isHash()) {
                    formatKey = key;
                }
                SpringUtil.getBean(RedisService.class).
                        save(random, randomRange, cacheName, cachePut.isHash(), formatKey, setValue, timout, timeUnit);
            }
        } finally {
            setOne(null);
        }

    }

}
