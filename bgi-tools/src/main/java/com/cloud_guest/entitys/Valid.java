package com.cloud_guest.entitys;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 实体校验器注册中心。
 *
 * @Author yan
 * @Date 2026/9/1 18:33:14
 * @Description
 */
@Slf4j
public class Valid {
    /**
     * 工具类，禁止实例化
     */
    private Valid() {
    }

    /**
     * 校验器记录。
     *
     * @param key  组合键
     * @param type 实体类型
     */
    public record Validator(
            @Schema(description = "组合键") String key,
            @Schema(description = "实体类型") Class<?> type
    ) {
    }

    /**
     * 校验器存储，使用类型安全的泛型注册方式，避免外部直接修改
     */
    private static final Map<Validator, Consumer<?>> VALIDATORS = new LinkedHashMap<>();

    /**
     * 构建组合键。
     *
     * @param source
     * @param type
     * @return
     */
    public static String buildKey(String source, Class<?> type) {
        source = source == null ? "default" : source;
        return source + ":" + type.getName();
    }

    /**
     * 注册校验器。
     *
     * @param type      实体类型
     * @param validator 对应类型的校验器
     * @param <T>       实体类型
     */
    public static <T> void register(Class<T> type, Consumer<T> validator) {
        register(buildKey(null, type), type, validator);
    }

    /**
     * @param key
     * @param type
     * @param validator
     * @param <T>
     */
    public static <T> void register(String key, Class<T> type, Consumer<T> validator) {
        Consumer<T> consumer = getValidator(key, type);
        if (consumer != null) {
            log.warn("[{}]:key={}, type={} 重复注册", "注册-类校验器",key, type);
            return;
        }
        log.debug("[{}]:key={}, type={}", "注册-类校验器",key, type);
        VALIDATORS.put(new Validator(key, type), validator);
    }

    /**
     * 获取校验器。
     *
     * @param type 实体类型
     * @param <T>  实体类型
     * @return 对应类型的校验器，不存在时返回 null
     */
    @SuppressWarnings("unchecked")

    public static <T> Consumer<T> getValidator(Class<T> type) {
        return getValidator(buildKey(null, type), type);
    }

    /**
     * @param key
     * @param type
     * @param <T>
     * @return
     */
    public static <T> Consumer<T> getValidator(String key, Class<T> type) {
        Validator valid = new Validator(key, type);
        return (Consumer<T>) VALIDATORS.get(valid);
    }

    /**
     * 返回只读的校验器视图，防止外部修改。
     */
    public static Map<Validator, Consumer<?>> validators() {
        return Collections.unmodifiableMap(VALIDATORS);
    }

    /**
     * 使用校验器对指定对象进行校验。
     *
     * @param type
     * @param t
     * @param <T>
     */
    public static <T> void validate(Class<T> type, T t) {
        validate(buildKey(null, type), type, t);
    }

    /**
     * 使用校验器对指定对象进行校验。
     *
     * @param key
     * @param type
     * @param t
     * @param <T>
     */
    public static <T> void validate(String key, Class<T> type, T t) {
        Consumer<T> handler = getValidator(key, type);
        if (handler == null) {
            throw new IllegalStateException("未注册校验器: key= " + key + ", type= " + type.getName());
        }
        handler.accept(t);
    }
}