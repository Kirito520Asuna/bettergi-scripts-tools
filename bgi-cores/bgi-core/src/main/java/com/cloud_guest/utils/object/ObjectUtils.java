package com.cloud_guest.utils.object;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author yan
 * @Date 2024/11/2 下午9:20:38
 * @Description
 */
public class ObjectUtils extends ObjectUtil{
    public static <T> T defaultIfEmpty(T object, T defaultValue) {
        return ObjectUtil.isEmpty(object) ? defaultValue : object;
    }

    /**
     * @param compareTo
     * @return
     */
    public static Map<String, Object> mapKeyLengthReversed(Map<String, Object> compareTo) {
        TreeMap<String, Object> treeMap = new TreeMap<>(Comparator.comparingInt(String::length).reversed().thenComparing(String::compareTo));
        treeMap.putAll(compareTo);
        return treeMap;
    }

    /**
     * @param compareTo
     * @return
     */
    public static Map<String, Object> mapKeyLengthSort(Map<String, Object> compareTo) {
        TreeMap<String, Object> treeMap = new TreeMap(Comparator.comparingInt(String::length).thenComparing(String::compareTo));
        treeMap.putAll(compareTo);
        return treeMap;
    }

    public static Map<String, Object> toMap(Object obj) {
        return toMap(null, obj, null, null);
    }

    /**
     * obj 转 map
     * obj:{"a":"a","b":"{\"a\":\"a\",\"b\":\"a\"}","c":"[{\"a\":\"a\",\"b\":\"a\"}]"}
     * ==>
     * map:{"a":"a","b.a":"a","b.b":"a","c[0].a":"a","c[0].b":"a"}
     *
     * @param map
     * @param obj
     * @param prx
     * @param level
     * @return
     */
    public static Map<String, Object> toMap(Map<String, Object> map, Object obj, String prx, Integer level) {
        JSONConfig jsonConfig = JSONConfig.create().setIgnoreNullValue(false);
        prx = StrUtil.isBlank(prx) ? StrUtil.EMPTY : prx;
        map = ObjectUtils.defaultIfNull(map, new LinkedHashMap<>());
        level = ObjectUtils.defaultIfNull(level, 0);

        String jsonObjectJsonStr = JSONUtil.toJsonStr(obj, jsonConfig);
        if (JSONUtil.isTypeJSON(jsonObjectJsonStr)) {
            JSONObject jsonObject = new JSONObject(jsonObjectJsonStr);
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String fullKey = StrUtil.isBlank(prx) ? key : prx + "." + key;

                String toJsonStr = String.valueOf(value);
                if (JSONUtil.isTypeJSONArray(toJsonStr)) {
                    // Handle JSONArray type
                    JSONArray array = new JSONArray(toJsonStr);
                    for (int i = 0; i < array.size(); i++) {
                        Object arrayValue = array.get(i);
                        level++;
                        toMap(map, arrayValue, fullKey + "[" + i + "]", level);
                        level--;
                    }
                } else if (JSONUtil.isTypeJSON(toJsonStr)) {
                    // Handle nested JSON object
                    level++;
                    toMap(map, value, fullKey, level);
                    level--;
                } else {
                    // Handle simple value
                    map.put(fullKey, value);
                    prx = removeLastSegment(prx);
                }
            }
        } else {
            // Handle non-JSON value
            map.put(prx, obj);
            prx = StrUtil.EMPTY;
        }

        return map;
    }

    public static String removeLastSegment(String prx) {
        String separator = ".";
        if (prx.contains(separator)) {
            int lastIndex = prx.lastIndexOf(separator);
            return prx.substring(0, lastIndex);
        }
        return prx;
    }

    public static Map<String, Object> blankReplace(Map<String, Object> objMap) {
        objMap = ObjectUtils.defaultIfEmpty(objMap, Maps.newLinkedHashMap());
        for (Map.Entry<String, Object> entry : objMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (ObjectUtils.equals(StrUtil.EMPTY, value)) {
                objMap.put(key, "''");
            }
        }
        return objMap;
    }


}
