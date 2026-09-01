package com.cloud_guest.entitys;

import com.cloud_guest.entitys.records.UidTeam;
import com.cloud_guest.exception.exceptions.GlobalException;

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
public class Valid {

    /** 校验器存储，使用类型安全的泛型注册方式，避免外部直接修改 */
    private static final Map<Class<?>, Consumer<?>> VALIDATORS = new LinkedHashMap<>();

    /** 工具类，禁止实例化 */
    private Valid() {
    }

    /**
     * 注册校验器。
     *
     * @param type      实体类型
     * @param validator 对应类型的校验器
     * @param <T>       实体类型
     */
    public static <T> void register(Class<T> type, Consumer<T> validator) {
        VALIDATORS.put(type, validator);
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
        return (Consumer<T>) VALIDATORS.get(type);
    }

    /**
     * 返回只读的校验器视图，防止外部修改。
     */
    public static Map<Class<?>, Consumer<?>> validators() {
        return Collections.unmodifiableMap(VALIDATORS);
    }

    /**
     * 使用校验器对指定对象进行校验。
     * @param type
     * @param t
     * @param <T>
     */
    public static <T> void validate(Class<T> type, T t) {
        Consumer<T> handler = getValidator(type);
        if (handler == null) {
            throw new IllegalStateException("未注册校验器: " + type.getName());
        }
        handler.accept(t);
    }

    static {
        // 注册 UidTeam 的校验器，lambda 参数类型会被编译器推断为 UidTeam
        register(UidTeam.class, uidTeam -> {
            String team = uidTeam.team();
            String uid = uidTeam.uid();
            String type = uidTeam.type();
            if (team == null || team.isBlank()) {
                throw new GlobalException("team 不能为空");
            }
            if (uid == null || uid.isBlank()) {
                throw new GlobalException("uid 不能为空");
            }
            if (type == null || type.isBlank()) {
                throw new GlobalException("type 不能为空");
            }
        });


    }


}