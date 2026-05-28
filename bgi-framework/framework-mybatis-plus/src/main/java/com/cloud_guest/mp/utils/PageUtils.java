package com.cloud_guest.mp.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author yan
 * @DateTime 2024/6/22 14:35:51
 * @Description
 */
public class PageUtils {
    public static final String pageSize = "pageSize";
    public static final String pageNumber = "pageNumber";

    /**
     * 分页  as_page_number 小驼峰自动转化 将新增一条 asPageNumber
     *
     * @param map
     * @return
     */
    public static Map<String, Object> startPage(Map<String, Object> map) {
        if (ObjectUtil.isNotEmpty(map)) {
            map.putAll(toCamelCase(map));
            Object o = map.get(pageNumber);
            Object o1 = map.get(pageSize);
            if (ObjectUtil.isNotEmpty(o) && ObjectUtil.isNotEmpty(o1)) {
                Integer pageNumber = null;
                Integer pageSize = null;
                if (o instanceof Integer number && o1 instanceof Integer size) {
                    pageNumber = number;
                    pageSize = size;
                    //PageHelper.startPage(pageNumber, pageSize);
                } else if (o instanceof Long number && o1 instanceof Long size) {
                    pageNumber = number.intValue();
                    pageSize = size.intValue();
                    //PageHelper.startPage(pageNumber.intValue(), pageSize.intValue());
                }

                if (ObjectUtil.isNotEmpty(pageNumber)&&ObjectUtil.isNotEmpty(pageSize)){
                    PageHelper.startPage(pageNumber, pageSize);
                }
            }
        }
        return map;
    }

    public static Map<String, Object> toCamelCase(Map<String, Object> map) {
        if (ObjectUtil.isNotEmpty(map)) {
            Map<String, Object> hashMap = new LinkedHashMap<>(map);
            Map<String, Object> finalMap = map;
            map.keySet().stream()
                    .forEach(key -> {
                        Object o = finalMap.get(key);
                        if (key.contains("_")) {
                            hashMap.put(StrUtil.toCamelCase(key), o);
                        }
                    });
            map.putAll(hashMap);
        }
        return map;
    }

}
