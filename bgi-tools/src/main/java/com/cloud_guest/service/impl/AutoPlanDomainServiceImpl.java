package com.cloud_guest.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.service.AutoPlanDomainService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.object.ObjectUtils;
import com.cloud_guest.vo.AutoPlanDomainVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/8 15:31:57
 * @Description
 */
@Service
public class AutoPlanDomainServiceImpl implements AutoPlanDomainService {
    private static final String key = "AUTO_PLAN_DOMAIN:UID:";
    private static final String key_all = "AUTO_PLAN_DOMAIN:ALL";
    @Resource
    private CacheService cacheService;

    @Override
    public boolean delList(List<String> ids) {
        ids = ids.stream().map(id -> key + id).collect(Collectors.toList());
        return cacheService.delList(ids);
    }

    @Override
    public boolean save(String id, String json) {
        id = key + id;
        return cacheService.save(id, json);
    }


    @Override
    public List<AutoPlanDomainVo> find(String id) {
        id = key + id;
        List<Map<String, Object>> list = new ArrayList<>();
        Cache<String> cache = cacheService.find(id);
        if (ObjectUtils.equals(cache.getType(), "json")) {
            String data = cache.getData();
            if (JSONUtil.isTypeJSONArray(data)) {
                List<String> maps = JSONUtil.toList(data, String.class);
                for (String json : maps) {
                    if (JSONUtil.isTypeJSONArray(json)){
                        // 解析为 JSONArray
                        List<JSONObject> maps1 = JSONUtil.toList(data, JSONObject.class);
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
        ObjectMapper bean = SpringUtil.getBean(ObjectMapper.class);
        List<AutoPlanDomainVo> collect = list.stream().map(map -> {
            return bean.convertValue(map, AutoPlanDomainVo.class);
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<Map<String, Object>> findAll() {
        List<Map<String, Object>> list = new ArrayList<>();
        Cache<String> cache = cacheService.find(key_all);
        if (ObjectUtils.equals(cache.getType(), "json")) {
            String data = cache.getData();
            if (JSONUtil.isTypeJSONArray(data)) {
                List<String> maps = JSONUtil.toList(data, String.class);
                for (String json : maps) {
                    if (JSONUtil.isTypeJSONArray(json)){
                        // 解析为 JSONArray
                        List<JSONObject> maps1 = JSONUtil.toList(data, JSONObject.class);
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

    @Override
    public boolean saveAll(String json) {
        return cacheService.save(key_all, json);
    }
}
