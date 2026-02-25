package com.cloud_guest.utils.bean;

import cn.hutool.core.bean.BeanUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author yan
 * @Date 2024/6/19 0019 11:53:30
 * @Description
 */
public class CustomBeanUtils extends BeanUtil {

    /**
     * 复制源对象到目标对象，仅复制非null属性。
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        // 获取源对象中所有的属性名
        String[] ignoreProperties = getNullPropertyNames(source);
        // 执行复制操作，忽略null值属性
        BeanUtil.copyProperties(source, target, ignoreProperties);
    }

    /**
     * 获取源对象中值为null的属性名数组。
     *
     * @param source 源对象
     * @return 值为null的属性名数组
     */
    private static String[] getNullPropertyNames(Object source) {
        Map<String, Object> beanToMap = BeanUtil.beanToMap(source);

        Set<String> emptyNames = new HashSet<>();
        Set<String> keySet = beanToMap.keySet();
        keySet.forEach(key -> {
            Object o = beanToMap.get(key);
            if (o == null) {
                emptyNames.add(key);
            }
        });
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
