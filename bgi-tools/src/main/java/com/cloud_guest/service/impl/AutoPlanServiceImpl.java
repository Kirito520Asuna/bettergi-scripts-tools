package com.cloud_guest.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.service.AutoPlanService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.vo.AutoPlanVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/8 15:31:57
 * @Description
 */
@Service
public class AutoPlanServiceImpl implements AutoPlanService {
    //private static final String key_uid_all = "AUTO_PLAN_UID:ALL";
    //private static final String KeyConstants.auto_plan_key_domain_all = "AUTO_PLAN_DOMAIN:ALL";
    //private static final String KeyConstants.auto_plan_key_country_all = "AUTO_PLAN_COUNTRY:ALL";
    @Resource
    private CacheService cacheService;

    @Override
    public boolean delList(List<String> ids) {
        ids = ids.stream().map(id -> KeyConstants.auto_plan_key + id).collect(Collectors.toList());
        return cacheService.removeList(ids);
    }

    @Override
    public boolean save(String id, String json) {
        id = KeyConstants.auto_plan_key + id;
        return cacheService.save(id, json);
    }


    @Override
    public List<AutoPlanVo> find(String id) {
        id = KeyConstants.auto_plan_key + id;
        Cache<String> cache = cacheService.find(id);
        List<Map<String, Object>> list =  cache.toList();

        ObjectMapper bean = SpringUtil.getBean(ObjectMapper.class);
        List<AutoPlanVo> collect = list.stream().map(map -> {
            return bean.convertValue(map, AutoPlanVo.class);
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<Map<String, Object>> findDomainAll() {
        Cache<String> cache = cacheService.find(KeyConstants.auto_plan_key_domain_all);
        return cache.toList();
    }

    @Override
    public boolean saveDomainAll(String json) {
        return cacheService.save(KeyConstants.auto_plan_key_domain_all, json);
    }

    @Override
    public boolean saveCountryAll(String json) {
        return cacheService.save(KeyConstants.auto_plan_key_country_all, json);
    }
    @Override
    public List<Map<String, Object>> findCountryAll() {
        Cache<String> cache = cacheService.find(KeyConstants.auto_plan_key_country_all);
        return cache.toList();
    }
}
