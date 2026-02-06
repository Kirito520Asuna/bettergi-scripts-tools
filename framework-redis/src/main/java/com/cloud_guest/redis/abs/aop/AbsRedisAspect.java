package com.cloud_guest.redis.abs.aop;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.redis.aop.order.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ValueNode;
import javax.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.internal.Engine;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * @Author yan
 * @Date 2024/5/24 0024 10:00
 * @Description
 */
public interface AbsRedisAspect extends Ordered {
    JSONConfig JSON_CONFIG = new JSONConfig().setIgnoreNullValue(false);
    /*#####################################################################################################################################*/
    /*常量模块*/
    // templateKey 缓存模板   placeholder 占位符   splicer 拼接符
    String templateKey = "%s:%s", placeholder = "#", splicer = "+";
    // 比较运算符
    List<String> comparisonOperators = Arrays.stream("> < = >= <= != + - * / % & | ^ ! ? :".split(" ")).collect(Collectors.toList());
    // 条件截取剔除
    List<String> conditionOperators = Arrays.stream("( ) { }".split(" ")).collect(Collectors.toList());

    @Override
    default int getOrder() {
        return Order.DEFAULT_ORDER;
    }

    /*#####################################################################################################################################*/
    /*记录实体模块*/
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    class RedisCacheParameters {
        /**
         * 缓存key请求参数
         */
        private Map<String, Object> request;
        /**
         * 缓存key响应参数
         */
        private Map<String, Object> response;
    }

    /**
     * 获取请求参数设置请求参数
     *
     * @param redisCacheParameters
     * @param joinPoint
     * @return
     */
    default RedisCacheParameters setRequesRedisCacheParameters(RedisCacheParameters redisCacheParameters, JoinPoint joinPoint) throws JsonProcessingException {
        Map<String, Object> map = new LinkedHashMap<>();
        Object[] pointArgs = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        ObjectMapper mapper = SpringUtil.getBean(ObjectMapper.class);
        for (int i = 0; i < parameterNames.length; i++) {
            Object pointArg = pointArgs[i];
            String json;
            try {
                json = mapper.writeValueAsString(pointArg);
            } catch (JsonMappingException e) {
                json = String.valueOf(pointArg);
            }
            Object bean;
            if (JSONUtil.isTypeJSON(json)) {
                Map<String,Object> map1 = JSONUtil.toBean(json, Map.class);
                bean = map1;
            }else {
                bean = pointArg;
            }

            map.put(parameterNames[i], bean);
        }
        return redisCacheParameters.setRequest(map);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    class RedisEntity<T> {
        private T entity;
        private RedisCacheParameters redisCacheParameters;
    }

    @Getter
    @AllArgsConstructor
    enum OperationType {
        str, condition
    }
    /*#####################################################################################################################################*/
    /*aop模块*/

    default <T extends Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> tClass) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        T cachePut = method.getAnnotation(tClass);
        return cachePut;
    }


    /**
     * 切面方法
     */
    default void pointcutAspect() {
    }

    /**
     * 前置通知
     *
     * @param joinPoint
     */
    @SneakyThrows
    default void doBefore(JoinPoint joinPoint) {
    }

    /**
     * 环绕通知
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    default Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //目标方法执行
        return joinPoint.proceed();
    }

    /**
     * 后置通知
     *
     * @param joinPoint
     * @param result
     */
    @SneakyThrows
    default void afterReturning(JoinPoint joinPoint, Object result) {
    }

    default String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果通过多层代理，X-Forwarded-For的值中可能会有多个IP地址，第一个IP地址才是真实的客户端IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
    /*#####################################################################################################################################*/
    /*拼接模块*/

    /**
     * 拼接有效Key
     *
     * @param key
     * @return
     */
    default String effectiveSplicingString(String key, JSONObject json, Collection<String> collection, OperationType operationType) {
        boolean contains = key.contains(placeholder);
        boolean conditionType = ObjectUtil.equals(operationType, OperationType.condition);

        //占位符名称==>占位符值
        Map<String, Object> placeholderMap = new LinkedHashMap();
        List<String> arrayList = CollUtil.isEmpty(collection) ? CollUtil.newArrayList() : CollUtil.newArrayList(collection);
        if (contains) {
            //占位符
            List<String> keys = Arrays.stream(key.split(placeholder)).filter(StrUtil::isNotBlank).collect(Collectors.toList());
            if (CollUtil.isEmpty(keys)) {
                keys = CollUtil.newArrayList();
            } else {
                String firstStr = keys.get(0);
                if (key.startsWith(firstStr)) {
                    keys.remove(firstStr);
                }
            }
            AtomicReference<String> conditionTypeKey = new AtomicReference<>(key.replace(conditionType ? "" : splicer, ""));
            //处理拼接
            keys.stream().forEach(key1 -> {
                int index = key1.length();
                if (CollUtil.isNotEmpty(arrayList)) {
                    index = arrayList.stream()
                            .map(containKey -> key1.indexOf(containKey)).filter(i -> i > 0)
                            .mapToInt(i -> i).min().orElse(index);
                }
                String op = key1.substring(0, index);
                boolean conditionTypeBool = false;
                for (String operator : conditionOperators) {
                    if (op.startsWith(operator) || op.endsWith(operator)) {
                        conditionTypeBool = true;
                        break;
                    }
                }

                if (conditionType || conditionTypeBool) {
                    AtomicReference<String> conditionTypeOp = new AtomicReference<>(op);
                    conditionOperators.stream().forEach(op1 -> {
                        String s = conditionTypeOp.get();
                        conditionTypeOp.set(s.replace(op1, ""));
                    });
                    op = conditionTypeOp.get();
                }

                Object byPath = json.getByPath(op);

                if (conditionType) {
                    String str = byPath.toString();
                    boolean typeJSON = JSONUtil.isTypeJSON(str);
                    byPath = typeJSON ? new StringBuffer("'").append(str).append("'").toString() : str;
                }
                placeholderMap.put(placeholder + op, byPath);
            });
            key = conditionTypeKey.get();
        }
        Map<String, String> jsonMap = new LinkedHashMap<>();

        Iterator<String> iterator = placeholderMap.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (conditionType) {
                AtomicReference<String> conditionTypeOp = new AtomicReference<>(next);
                conditionOperators.stream().forEach(op1 -> {
                    String s = conditionTypeOp.get();
                    conditionTypeOp.set(s.replace(op1, ""));
                });
                next = conditionTypeOp.get();
            }
            Object obj = placeholderMap.get(next);
            JsonNode jsonNode = SpringUtil.getBean(ObjectMapper.class).valueToTree(obj);
            String objStr;
            if (jsonNode instanceof ValueNode) {
                objStr = String.valueOf(obj);
            } else {
                objStr = JSONUtil.toJsonStr(obj);
            }
            //String jsonStr = JSONUtil.toJsonStr(obj);

            if (!key.contains(next)) {
                String mode = placeholder + "%s%s%s";
                String nextMode = next.replace(placeholder, "");

                List<String> collect = Arrays.stream("( ),{ }".split(",")).collect(Collectors.toList());
                for (String str : collect) {
                    String[] split = str.split(" ");
                    String format = String.format(mode, split[0], nextMode, split[1]);
                    if (key.contains(format)) {
                        next = format;
                        break;
                    }
                }
            }
            jsonMap.put(next, objStr);
        }

        if (CollUtil.isNotEmpty(jsonMap)) {
            List<String> jsonKeys = CollUtil.newArrayList(jsonMap.keySet())
                    .stream()
                    .sorted((s1, s2) -> s2.length() - s1.length())
                    .collect(Collectors.toList());
            for (String jsonKey : jsonKeys) {
                key = key.replace(jsonKey, jsonMap.get(jsonKey));
            }
        }
        if (conditionType) {
        } else {
            key = key.replace(splicer + "'", "")
                    .replace("'" + splicer, "")
                    .replace("'", "");
        }
        return key;
    }
    /*#####################################################################################################################################*/

    default String replaceKey(String key, String[] replaceSplicingList) {
        return key.replace(replaceSplicingList[0], replaceSplicingList[1]);
    }


    /**
     * 判断是否是条件
     *
     * @param condition
     * @return
     */
    default boolean verifiedOkCondition(String condition) {
        // 创建JexlEngine实例 解除jdk 8 强制要求
        JexlEngine jexl = new Engine();
        JexlExpression expression = jexl.createExpression(condition);
        boolean evalResult = (Boolean) expression.evaluate(null);
        return evalResult || Boolean.parseBoolean(condition);
    }
}
