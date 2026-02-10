package com.cloud_guest.utils;

import cn.hutool.core.util.IdUtil;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @Author yan
 * @Date 2024/5/14 0014 14:57
 * @Description
 */
public final class ThreadMdcUtil {
    public static final String TRACE_ID = "TRACE_ID";

    /**
     * 生成traceId
     *
     * @return
     */
    public static String generateTraceId() {
        return System.currentTimeMillis() + "^" + IdUtil.getSnowflakeNextIdStr();
    }

    /**
     * 获取当前线程的MDC信息
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    /**
     * 设置当前线程的MDC信息
     *
     * @param key
     * @param value
     */
    public static void put(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * 移除当前线程的MDC信息 key
     *
     * @param key
     */
    public static void remove(String key) {
        MDC.remove(key);
    }

    /**
     * 清除当前线程的MDC信息
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * 获取当前线程的MDC信息
     *
     * @return
     */
    public static Map getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * 设置当前线程的MDC信息
     *
     * @param contextMap
     */
    public static void setContextMap(Map contextMap) {
        MDC.setContextMap(contextMap);
    }

    /**
     * 获取当前线程的traceId
     *
     * @return
     */
    public static String getTraceId() {
        String traceId = get(TRACE_ID);
        if (traceId == null) {
            traceId = generateTraceId();
            setTraceId(traceId);
        }
        return traceId;
    }

    /**
     * 设置traceId
     *
     * @param traceId
     */
    public static void setTraceId(String traceId) {
        put(TRACE_ID, traceId);
    }

    /**
     * 移除traceId
     */
    public static void removeTraceId() {
        remove(TRACE_ID);
    }

    /**
     * 设置traceId，如果为空则生成一个
     */
    public static void setTraceIdIfAbsent() {
        if (getTraceId() == null) {
            setTraceId(generateTraceId());
        }
    }

    /**
     * 用于父线程向线程池中提交任务时，将自身MDC中的数据复制给子线程
     *
     * @param callable
     * @param context
     * @param <T>
     * @return
     */
    public static <T> Callable<T> wrap(final Callable<T> callable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                clear();
            } else {
                setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                return callable.call();
            } finally {
                clear();
            }
        };
    }

    /**
     * 用于父线程向线程池中提交任务时，将自身MDC中的数据复制给子线程
     *
     * @param runnable
     * @param context
     * @return
     */
    public static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                clear();
            } else {
                setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                runnable.run();
            } finally {
                clear();
            }
        };
    }
}