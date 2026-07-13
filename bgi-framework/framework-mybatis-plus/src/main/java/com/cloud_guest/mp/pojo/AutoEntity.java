package com.cloud_guest.mp.pojo;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author yan
 * @Date 2026/7/13 11:04:15
 * @Description
 */
@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class AutoEntity<T> {
    // 字段名
    private String key;
    // 字段值
    private T value;
    // 字段类型
    private Class<T> type;
    // 是否插入
    private boolean insert = false;
    // 是否更新
    private boolean update = false;

    /**
     * 将键转换为驼峰命名格式的字符串
     *
     * @return 返回转换后的驼峰命名格式的字符串
     */
    public String getCamelCaseKey() {
        // 使用StrUtil工具类的toCamelCase方法将key转换为驼峰命名格式
        return StrUtil.toCamelCase(key);
    }

    /**
     * 将当前key转换为下划线命名格式的字符串
     * 例如：将"camelCase"转换为"camel_case"
     *
     * @return 返回转换后的下划线命名格式的key字符串
     */
    public String getUnderlineCaseKey() {
        // 使用StrUtil工具类将当前key转换为下划线命名格式
        return StrUtil.toUnderlineCase(key);
    }
}
