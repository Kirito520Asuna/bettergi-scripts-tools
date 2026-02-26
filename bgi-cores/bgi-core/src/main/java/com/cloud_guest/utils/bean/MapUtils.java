package com.cloud_guest.utils.bean;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @Author yan
 * @Date 2026/2/26 21:19:17
 * @Description
 */
public class MapUtils {
    /**
     * 根据点分隔的路径创建层次化Map
     * @param path 点分隔的路径，如 "a.b.c"
     * @param value 叶子节点的值
     * @return 层次化Map结构
     */
    public static Map<String, Object> createHierarchicalMap(String path, Object value) {
        Map<String, Object> result = Maps.newLinkedHashMap();
        String[] parts = path.split("\\.");

        if (parts.length == 0) {
            return result;
        }

        Map<String, Object> current = result;
        for (int i = 0; i < parts.length - 1; i++) {
            Map<String, Object> next = Maps.newLinkedHashMap();
            current.put(parts[i], next);
            current = next;
        }

        current.put(parts[parts.length - 1], value);
        return result;
    }

    /**
     * 创建分层结构的Map，使用默认的空值作为第二个参数
     * @param path 用于构建Map结构的路径字符串
     * @return 返回一个包含层次结构的Map对象，其中键为路径中的节点，值为子Map或null
     */
    public static Map<String, Object> createHierarchicalMap(String path) {
        // 调用重载方法，传入null作为第二个参数
        return createHierarchicalMap(path, null);
    }
}
