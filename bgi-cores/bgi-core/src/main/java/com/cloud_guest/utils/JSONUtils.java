package com.cloud_guest.utils;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/6/18 15:39:28
 * @Description
 */
public class JSONUtils {
    /**
     * 将点分路径转换为嵌套JSONObject
     * sign.key.xxx + value ==> {sign:{key:{xxx:value}}}
     *
     * @param key   点分路径，如 "sign.key.xxx"
     * @param value 最终值
     * @return 嵌套的JSONObject
     */
    public static JSONObject toJSONObject(String key, String value) {
        if (StrUtil.isBlankIfStr(key)) {
            return new JSONObject();
        }

        String[] parts = key.split("\\.");
        JSONObject root = new JSONObject();
        JSONObject current = root;

        for (int i = 0; i < parts.length; i++) {
            if (i == parts.length - 1) {
                current.put(parts[i], value);
            } else {
                JSONObject next = new JSONObject();
                current.put(parts[i], next);
                current = next;
            }
        }

        return root;
    }

    /**
     * 将输入的JSONObject对象进行处理，移除指定的键值对，并返回处理后的JSONObject
     *
     * @param jsonObject 需要处理的JSONObject对象，如果为null则创建一个新的空JSONObject
     * @param keyExList  需要移除的键的列表，可变参数形式，支持传入多个键
     * @return 处理后的JSONObject对象，已移除指定的键值对
     */
    public static JSONObject toJSONObject(JSONObject jsonObject, List<String> keyExList) {
        // 如果输入的JSONObject为null，则创建一个新的空JSONObject
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        // 遍历所有需要移除的键
        for (String key : keyExList) {
            // 检查键是否为空或空白字符串
            if (!StrUtil.isBlankIfStr(key)) {
                // 调用removeByPath方法移除指定键对应的键值对
                removeByPath(jsonObject, key);
            }
        }
        // 返回处理后的JSONObject
        return jsonObject;
    }


    /**
     * 按点分路径移除JSONObject中的键
     *
     * @param jsonObject JSONObject
     * @param path       点分路径，如 "sign.key.xxx"
     */
    public static JSONObject removeByPath(JSONObject jsonObject, String path) {
        if (StrUtil.isBlankIfStr(path)) {
            return jsonObject;
        }

        String[] parts = path.split("\\.");
        if (parts.length == 1) {
            jsonObject.remove(path);
            return jsonObject;
        }

        JSONObject current = jsonObject;
        for (int i = 0; i < parts.length - 1; i++) {
            Object obj = current.get(parts[i]);
            if (obj instanceof JSONObject) {
                current = (JSONObject) obj;
            } else {
                return jsonObject;
            }
        }

        current.remove(parts[parts.length - 1]);
        return jsonObject;
    }

    /**
     * 深度合并两个JSONObject
     *
     * @param target 目标JSONObject
     * @param source 源JSONObject
     */
    public static void deepMerge(JSONObject target, JSONObject source) {
        if (source == null || target == null) {
            return;
        }

        for (String key : source.keySet()) {
            Object sourceValue = source.get(key);
            Object targetValue = target.get(key);

            if (sourceValue instanceof JSONObject && targetValue instanceof JSONObject) {
                deepMerge((JSONObject) targetValue, (JSONObject) sourceValue);
            } else {
                target.put(key, sourceValue);
            }
        }

    }
/*    public static void main(String[] args) {
        JSONObject entries = toJSONObject("sign.key.api", "api");
        JSONObject entries1 = toJSONObject("sign.key.jwt", "jwt");
        JSONObject entries2 = toJSONObject("sign.key.other", "other");

        deepMerge(entries, entries1);
        deepMerge(entries, entries2);
        System.err.println(JSONUtil.toJsonStr(entries));
        entries = removeByPath(entries, "sign.key.api");
        System.err.println(JSONUtil.toJsonStr(entries));
    }*/
}
