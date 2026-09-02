package com.cloud_guest.entitys;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author yan
 * @Date 2026/9/2 21:17:03
 * @Description 类型转换注册中心，参照 Valid 工具类设计
 * key: source + target 组合，支持同一个源转换为多个不同目标类型
 */
@Slf4j
public class ClassConvert {

    /**
     * 工具类，禁止实例化
     */
    private ClassConvert() {
    }

    /**
     * 组合键：源类型 + 目标类型，作为 Map 的 key
     * record 自动实现 equals & hashCode，可直接用于HashMap/LinkedHashMap key
     */
    public record SourceTargetKey(
            @Schema(description = "组合键：源类型 + 目标类型") String key,
            @Schema(description = "源类型") Class<?> source,
            @Schema(description = "目标类型") Class<?> target
    ) {
    }

    /**
     * 转换器注册表
     * key = source+target组合键，value = 转换函数 Function<源,目标>
     */
    private static final Map<SourceTargetKey, Function<?, ?>> CONVERT_MAP = new LinkedHashMap<>();


    /**
     * 注册转换器
     *
     * @param key       组合键：源类型 + 目标类型
     * @param source    源类型Class
     * @param target    目标类型Class
     * @param converter 转换函数 source实例 -> target实例
     * @param <T>
     * @param <U>
     */
    public static <T, U> void register(String key, Class<T> source, Class<U> target, Function<T, U> converter) {
        register(key, source, target, converter, true);
    }

    /**
     * 注册转换器
     *
     * @param key       组合键：源类型 + 目标类型
     * @param source    源类型Class
     * @param target    目标类型Class
     * @param converter 转换函数 source实例 -> target实例
     * @param override  是否覆盖已注册的转换器
     * @param <T>
     * @param <U>
     */
    public static <T, U> void register(String key, Class<T> source, Class<U> target, Function<T, U> converter, boolean override) {
        Function<T, U> function = getConverter(key, source, target);
        if (function != null) {
            log.warn("[{}]:key={}, source={}, target={} 重复注册 {}", "注册-类转换器", key, source, target, override ? "覆盖" : "抛弃");
            if (!override) return;
        } else {
            log.debug("[{}]:key={}, source={}, target={}", "注册-类转换器", key, source, target);
        }
        SourceTargetKey sourceTargetKey = new SourceTargetKey(key, source, target);
        CONVERT_MAP.put(sourceTargetKey, converter);
    }

    /**
     * 注册转换器
     *
     * @param source    源类型Class
     * @param target    目标类型Class
     * @param converter 转换函数 source实例 -> target实例
     * @param <T>       源类型
     * @param <U>       目标类型
     */
    public static <T, U> void register(Class<T> source, Class<U> target, Function<T, U> converter) {
        String key = buildKey(source, target);
        register(key, source, target, converter);
    }


    /**
     * 注册转换器，包含反向转换
     *
     * @param source           源类型Class
     * @param target           目标类型Class
     * @param converter        转换函数 source实例 -> target实例
     * @param reverseConverter 反向转换函数 target实例 -> source实例
     * @param <T>
     * @param <U>
     */
    public static <T, U> void register(Class<T> source, Class<U> target, Function<T, U> converter, Function<U, T> reverseConverter) {
        register(null, source, target, converter, reverseConverter);
    }

    /**
     * @param key              组合键：源类型 + 目标类型
     * @param source           源类型Class
     * @param target           目标类型Class
     * @param converter        转换函数 source实例 -> target实例
     * @param reverseConverter 反向转换函数 target实例 -> source实例
     * @param <T>
     * @param <U>
     */
    public static <T, U> void register(String key, Class<T> source, Class<U> target, Function<T, U> converter, Function<U, T> reverseConverter) {
        register((key == null ? buildKey(source, target) : key), source, target, converter);
        register((key == null ? buildKey(target, source) : key), target, source, reverseConverter);
    }

    /**
     * 构建组合键：源类型 + 目标类型
     *
     * @param source
     * @param target
     * @return
     */
    public static String buildKey(Class<?> source, Class<?> target) {
        return source.getName() + ":" + target.getName();
    }

    /**
     * 获取转换器函数
     *
     * @param source 源类型
     * @param target 目标类型
     * @param <T>    源
     * @param <U>    目标
     * @return 转换器，不存在返回null
     */
    public static <T, U> Function<T, U> getConverter(Class<T> source, Class<U> target) {
        return getConverter(buildKey(source, target), source, target);
    }

    /**
     * 获取转换器函数
     *
     * @param key    组合键：源类型 + 目标类型
     * @param source 源类型
     * @param target 目标类型
     * @param <T>
     * @param <U>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T, U> Function<T, U> getConverter(String key, Class<T> source, Class<U> target) {
        SourceTargetKey sourceTargetKey = new SourceTargetKey(key, source, target);
        return (Function<T, U>) CONVERT_MAP.get(sourceTargetKey);
    }

    /**
     * 执行转换
     *
     * @param sourceCls 源class
     * @param targetCls 目标class
     * @param sourceObj 源对象实例
     * @param <T>       源类型
     * @param <U>       目标类型
     * @return 转换完成的目标对象
     */
    public static <T, U> U convert(Class<T> sourceCls, Class<U> targetCls, T sourceObj) {
        String key = buildKey(sourceCls, targetCls);
        return convert(key, sourceCls, targetCls, sourceObj);
    }

    /**
     * 执行转换
     *
     * @param key
     * @param sourceCls 源class
     * @param targetCls 目标class
     * @param sourceObj 源对象实例
     * @param <T>       源类型
     * @param <U>       目标类型
     * @return 转换完成的目标对象
     */
    public static <T, U> U convert(String key, Class<T> sourceCls, Class<U> targetCls, T sourceObj) {
        Function<T, U> converter = getConverter(key, sourceCls, targetCls);
        if (converter == null) {
            throw new IllegalStateException(
                    String.format("未注册转换器：key=%s,convert=%s → %s", key, sourceCls.getName(), targetCls.getName())
            );
        }
        return converter.apply(sourceObj);
    }

    /**
     * 返回只读视图，防止外部修改注册表，对齐 Valid#validators()
     *
     * @return 不可修改map
     */
    public static Map<SourceTargetKey, Function<?, ?>> converters() {
        return Collections.unmodifiableMap(CONVERT_MAP);
    }


    public static void main(String[] args) {

        register("test", String.class, String.class, str -> {
            System.err.println("convert: " + str);
            return str;
        });//注册转换器 指定 key
        System.err.println(convert("test", String.class, String.class, "test"));//✅️已注册转换器
        //System.err.println(convert(String.class, String.class, "test"));//❌️未注册转换器
    }
}
