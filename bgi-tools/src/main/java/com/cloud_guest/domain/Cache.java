package com.cloud_guest.domain;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.utils.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author yan
 * @Date 2026/2/6 18:27:11
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cache<T extends Object> {
    //类型
    private String type;
    //data
    private T data;

   public List<Map<String, Object>> toList() {
       List<Map<String, Object>> list = new ArrayList<>();
        if (ObjectUtils.equals(this.getType(), "json")) {
            T data1 = this.getData();
            String data="";
            if (data1 instanceof String) {
                data = (String) data1;
            }
            if (JSONUtil.isTypeJSONArray(data)) {
                List<String> maps = JSONUtil.toList(data, String.class);
                for (String json : maps) {
                    if (JSONUtil.isTypeJSONArray(json)){
                        // 解析为 JSONArray
                        List<JSONObject> maps1 = JSONUtil.toList(json, JSONObject.class);
                        list.addAll(maps1);
                    }else {
                        Map<String, Object> bean = JSONUtil.toBean(json, JSONObject.class);
                        list.add(bean);
                    }

                }
            } else
            if (JSONUtil.isTypeJSON(data)) {
                Map<String, Object> bean = JSONUtil.toBean(data, Map.class);
                list.add(bean);
            }
        }

        return list;
    }
}
