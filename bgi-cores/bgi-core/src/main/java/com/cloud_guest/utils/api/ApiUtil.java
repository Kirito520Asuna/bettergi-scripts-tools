package com.cloud_guest.utils.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cloud_guest.utils.object.ObjectUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author yan
 * @Date 2024/7/30 0030 9:58:52
 * @Description
 */
@Slf4j
public class ApiUtil {
    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    public static String generalSign(SingleSignature singleSignature) {
        String salt = singleSignature.getSalt();
        String method = singleSignature.getMethod();
        String url = singleSignature.getUrl();
        Map<String, String[]> params = singleSignature.getParams();
        Map<String, Object> body = singleSignature.getBody();
        Collection<String> exCollection = singleSignature.getExCollection();
        return generalSign(salt, method, url, params, body, exCollection);
    }

    @SneakyThrows
    public static String generalSign(String salt, String method, String url, Map<String, String[]> params, Map<String, Object> body, Collection<String> exCollection) {
        // 将排除参数转换为List，方便后续操作
        List<String> exList = ObjectUtils.defaultIfEmpty((List<String>) exCollection, CollUtil.newArrayList());
        // 构建用于生成签名的字符串
        StringBuilder stringBuilder = new StringBuilder().append(method.toUpperCase()).append(url);
        // 使用TreeMap对请求参数进行排序，保证签名的一致性
        Map<String, Object> hashMap = Maps.newLinkedHashMap();
        if (CollUtil.isNotEmpty(params)) {
            params.entrySet().stream().forEach(o -> {
                String key = o.getKey();
                String[] value = o.getValue();
                boolean aNull = ObjectUtils.equal(value[0], "null");
                String value1 = aNull ? null : value[0];
                if (value1 != null) {
                    hashMap.put(key, value1);
                }
            });
        } else if (CollUtil.isNotEmpty(body)) {
            body.entrySet().stream()
                    .forEach(o -> {
                        boolean aNull = ObjectUtils.equal(o.getValue(), "null");
                        Object value = o.getValue();
                        Object value1 = aNull ? null : value;
                        if (value1 != null) {
                            hashMap.put(o.getKey(), value1);
                        }
                    });
            //hashMap.putAll(body);
        }
        log.info("hashMap:{}", hashMap);
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.putAll(fieldForSort(hashMap));
        // 遍历排序后的请求参数，将参数名和值拼接成字符串
        if (ObjectUtil.isNotEmpty(treeMap)) {
            stringBuilder.append("?");
        }
        treeMap.keySet().stream().filter(e -> !exList.contains(e))
                .forEach(key -> stringBuilder.append(key).append("=").append(treeMap.get(key)).append("&"));
        // 追加签名密钥的属性值
        stringBuilder.append(salt);
        String preContent = stringBuilder.toString().replaceAll("\\\\+n", "\n").replaceAll("\\\\+r", "\r");
        // 对签名字符串进行URL编码
        byte[] bytes = URLEncoder.encode(preContent.toString(), StandardCharsets.UTF_8.name())
                .replace("+", "%20")
                .replace("*", "%2A")
                .getBytes();
        // 计算MD5摘要，返回十六进制字符串形式的摘要
        return DigestUtils.md5DigestAsHex(bytes);
    }

    public static Map<String, Object> fieldForSort(Map<String, Object> map) {
        JsonNode jsonNode = getObjectMapper().valueToTree(map);
        return fieldForSort(jsonNode);
    }

    public static Map<String, Object> fieldForSort(JsonNode jsonNode) {
        Map<String, Object> fieldMap = Maps.newLinkedHashMap();
        return fieldForSort(jsonNode, fieldMap);
    }

    // 定义一个方法 fieldForSort，从 JSON 节点中提取字段，并将它们存储在一个映射中。
    public static Map<String, Object> fieldForSort(JsonNode jsonNode, Map<String, Object> fieldMap) {
        // 判断 jsonNode 是否为 ArrayNode 类型
        if (jsonNode instanceof ArrayNode) {
            // 遍历数组节点下的所有元素，并对每个元素递归调用 fieldForSort 方法
            for (JsonNode item : jsonNode) {
                fieldForSort(item, fieldMap);
            }
        } else if (jsonNode instanceof ObjectNode) {
            // 遍历对象节点下的所有字段
            Iterator<Map.Entry<String, JsonNode>> iterator = jsonNode.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                // 如果字段的值是 ValueNode 类型
                if (fieldValue instanceof ValueNode) {
                    // 将该字段名和字段值添加到 fieldMap 中
                    fieldMap.put(fieldName, fieldValue.asText());
                } else if (fieldValue instanceof ObjectNode) {
                    // 如果字段的值是 ObjectNode 类型，则递归调用 fieldForSort 方法
                    fieldForSort(fieldValue, fieldMap);
                } else if (fieldValue instanceof ArrayNode) {
                    // 如果字段的值是 ArrayNode 类型，则遍历该数组节点下的所有元素，并对每个元素递归调用 fieldForSort 方法
                    for (JsonNode arrayItem : fieldValue) {
                        fieldForSort(arrayItem, fieldMap);
                    }
                }
            }
        }
        // 返回 fieldMap
        return fieldMap;
    }
}
