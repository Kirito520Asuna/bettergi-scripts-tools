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

}
