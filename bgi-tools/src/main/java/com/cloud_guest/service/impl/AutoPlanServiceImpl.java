package com.cloud_guest.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.mapper.AutoPlanMapper;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.mp.abs.service.impl.MpServiceImpl;
import com.cloud_guest.pojo.AutoPlanConfig;
import com.cloud_guest.pojo.DbKV;
import com.cloud_guest.service.AutoPlanService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.service.DbKVService;
import com.cloud_guest.utils.object.ObjectUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author yan
 * @Date 2026/2/8 15:31:57
 * @Description
 */
@Service
public class AutoPlanServiceImpl extends ServiceImpl<AutoPlanMapper, AutoPlanConfig> implements AutoPlanService {
    @Resource
    private AutoPlanMapper dao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByUidList(List<String> uidList) {
        LambdaQueryWrapper<AutoPlanConfig> query = Wrappers.lambdaQuery();
        query.in(AutoPlanConfig::getUid, uidList);
        return remove(query);
    }

    @Override
    public List<AutoPlanConfig> find(String uid, Boolean enable) {
        LambdaQueryWrapper<AutoPlanConfig> query = Wrappers.lambdaQuery();
        query.eq(AutoPlanConfig::getUid, uid)
                .eq(ObjectUtils.isNotEmpty(enable), AutoPlanConfig::getEnable, enable);

        return list(query);
    }

    @Override
    public List<String> findUidAll() {
        List<String> uidList = list().stream().map(AutoPlanConfig::getUid).distinct().toList();
        return uidList;
    }

//cache replace
    @Resource
    private CacheService cacheService;
    @Resource
    private DbKVService dbKVService;
    @Override
    public List<Map<String, Object>> findDomainAll() {
        LambdaQueryWrapper<DbKV> query = Wrappers.lambdaQuery(DbKV.class);
        query.eq(DbKV::getType, KeyConstants.auto_plan_key_domain_all);

        DbKV dbKV = dbKVService.getOne(query);
        List<JSONObject> objectList = Optional.ofNullable(dbKV).map(k -> {
            String value = k.getValue();
            if (ObjectUtils.isEmpty(value)){
                return new ArrayList<JSONObject>();
            }
            List<JSONObject> list = JSONUtil.toList(value, JSONObject.class);
            return list;
        }).orElse(new ArrayList<>());

        List<Map<String, Object>> list = new ArrayList<>();
        if (CollUtil.isNotEmpty(objectList)){
            list.addAll(objectList);
        }

        return list;
        //Cache<String> cache = cacheService.find(KeyConstants.auto_plan_key_domain_all);
        //return cache.toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDomainAll(String json) {
        DbKV dbKV = new DbKV();
        String id = KeyConstants.auto_plan_key_domain_all;
        dbKV.setType(id);
        dbKV.setKey(id);
        dbKV.setValue(json);

        dbKVService.remove(
                Wrappers.lambdaQuery(DbKV.class)
                        .eq(DbKV::getType, id)
                        .eq(DbKV::getKey, id)
        );

        return dbKVService.save(dbKV);
        //return cacheService.save(KeyConstants.auto_plan_key_domain_all, json);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCountryAll(String json) {
        DbKV dbKV = new DbKV();
        String id = KeyConstants.auto_plan_key_country_all;
        dbKV.setType(id);
        dbKV.setKey(id);
        dbKV.setValue(json);

        dbKVService.remove(
                Wrappers.lambdaQuery(DbKV.class)
                        .eq(DbKV::getType, id)
                        .eq(DbKV::getKey, id)
        );

        return dbKVService.save(dbKV);
        //return cacheService.save(KeyConstants.auto_plan_key_country_all, json);
    }

    @Override
    public List<String> findCountryAll() {
        LambdaQueryWrapper<DbKV> query = Wrappers.lambdaQuery(DbKV.class);
        query.eq(DbKV::getType, KeyConstants.auto_plan_key_country_all);

        DbKV dbKV = dbKVService.getOne(query);
        List<String> objectList = Optional.ofNullable(dbKV).map(k -> {
            String value = k.getValue();
            if (ObjectUtils.isEmpty(value)){
                return new ArrayList<String>();
            }
            List<String> list = JSONUtil.toList(value, String.class);
            return list;
        }).orElse(new ArrayList<>());

        List<String> list = new ArrayList<>();
        if (CollUtil.isNotEmpty(objectList)){
            list.addAll(objectList);
        }

        return list;
        //
        //Cache<String> cache = cacheService.find(KeyConstants.auto_plan_key_country_all);
        //return cache.toListByString();
    }
}
