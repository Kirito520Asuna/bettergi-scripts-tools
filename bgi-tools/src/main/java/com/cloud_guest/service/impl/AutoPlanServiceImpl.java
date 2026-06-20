package com.cloud_guest.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.mapper.AutoPlanMapper;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.entitys.pojo.AutoPlanConfig;
import com.cloud_guest.entitys.pojo.DbKV;
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
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
    public boolean saveOrUpdateBatchList(List<AutoPlanConfig> configList) {
        if (CollUtil.isNotEmpty(configList)) {
            AutoPlanConfig planConfig = configList.stream().findFirst().get();
            String uid = planConfig.getUid();

            // 获取传入的所有有效ID
            List<Long> ids = configList.stream()
                    .map(AutoPlanConfig::getId)
                    .filter(ObjectUtils::isNotEmpty)
                    .toList();

            // 查询数据库中该UID下的所有配置
            List<AutoPlanConfig> existingList = this.lambdaQuery()
                    .eq(AutoPlanConfig::getUid, uid)
                    .list();

            // 找出需要删除的配置：云端有但传入列表中没有的
            List<Long> deleteIds = existingList.stream()
                    .map(AutoPlanConfig::getId)
                    .filter(id -> !ids.contains(id))
                    .toList();

            // 先删除不需要的配置，再保存/更新传入的配置
            if (CollUtil.isNotEmpty(deleteIds)) {
                removeBatchByIds(deleteIds);
            }
            return saveOrUpdateBatch(configList);
        }
        return false;
    }

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
    public List<Map<String, Object>> findBossAll() {
        LambdaQueryWrapper<DbKV> query = Wrappers.lambdaQuery(DbKV.class);
        query.eq(DbKV::getType, KeyConstants.auto_plan_key_boss_all);

        DbKV dbKV = dbKVService.getOne(query);
        List<JSONObject> objectList = Optional.ofNullable(dbKV).map(k -> {
            String value = k.getValue();
            if (ObjectUtils.isEmpty(value)) {
                return new ArrayList<JSONObject>();
            }
            List<JSONObject> list = JSONUtil.toList(value, JSONObject.class);
            return list;
        }).orElse(new ArrayList<>());

        List<Map<String, Object>> list = new ArrayList<>();
        if (CollUtil.isNotEmpty(objectList)) {
            list.addAll(objectList);
        }

        return list;
    }

    @Override
    public List<Map<String, Object>> findDomainAll() {
        LambdaQueryWrapper<DbKV> query = Wrappers.lambdaQuery(DbKV.class);
        query.eq(DbKV::getType, KeyConstants.auto_plan_key_domain_all);

        DbKV dbKV = dbKVService.getOne(query);
        List<JSONObject> objectList = Optional.ofNullable(dbKV).map(k -> {
            String value = k.getValue();
            if (ObjectUtils.isEmpty(value)) {
                return new ArrayList<JSONObject>();
            }
            List<JSONObject> list = JSONUtil.toList(value, JSONObject.class);
            return list;
        }).orElse(new ArrayList<>());

        List<Map<String, Object>> list = new ArrayList<>();
        if (CollUtil.isNotEmpty(objectList)) {
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
        dbKV.setKeyName(id);
        dbKV.setValue(json);

        //dbKVService.remove(
        //        Wrappers.lambdaQuery(DbKV.class)
        //                .eq(DbKV::getType, id)
        //                .eq(DbKV::getKeyName, id)
        //);

        return dbKVService.saveOrUpdate(dbKV, Wrappers.lambdaQuery(DbKV.class).eq(DbKV::getType, id).eq(DbKV::getKeyName, id));
        //return cacheService.save(KeyConstants.auto_plan_key_domain_all, json);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBossAll(String json) {
        DbKV dbKV = new DbKV();
        String id = KeyConstants.auto_plan_key_boss_all;
        dbKV.setType(id);
        dbKV.setKeyName(id);
        dbKV.setValue(json);

        return dbKVService.saveOrUpdate(dbKV, Wrappers.lambdaQuery(DbKV.class).eq(DbKV::getType, id).eq(DbKV::getKeyName, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDomainAllByAdd(String json) {
        return addConfigsToDbKV(KeyConstants.auto_plan_key_domain_all, "name", json,
                Collectors.toMap(j -> j.getStr("name"), j -> j));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCountryAllByAdd(String json) {
        return addConfigsToDbKV(KeyConstants.auto_plan_key_country_all, "name", json,
                Collectors.toMap(j -> j.getStr("name"), j -> j));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBossAllByAdd(String json) {
        return addConfigsToDbKV(KeyConstants.auto_plan_key_boss_all, "name", json, null);
    }

    /**
     * 新增配置到指定 KV 键，同名配置以新增为准（去重合并）
     *
     * @param kvKey 数据库 KV 键，例如 KeyConstants.auto_plan_key_domain_all
     * @param json  新增配置的 JSON 数组字符串
     * @return 保存是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addConfigsToDbKV(String kvKey, String keyField, String json, Collector<JSONObject, ?, Map<String, JSONObject>> collector) {
        if (StrUtil.isBlank(json)) {
            log.warn("addConfigsToDbKV: json为空, key=" + kvKey);
            return false;
        }

        try {
            List<JSONObject> addList = JSONUtil.toList(json, JSONObject.class);
            if (CollUtil.isEmpty(addList)) {
                log.warn("addConfigsToDbKV: 解析后新增列表为空, key=" + kvKey);
                return false;
            }

            LambdaQueryWrapper<DbKV> query = Wrappers.lambdaQuery(DbKV.class)
                    .eq(DbKV::getType, kvKey)
                    .eq(DbKV::getKeyName, kvKey);

            DbKV existingKV = dbKVService.getOne(query);
            List<JSONObject> existingList = Optional.ofNullable(existingKV)
                    .map(k -> StrUtil.isNotBlank(k.getValue())
                            ? JSONUtil.toList(k.getValue(), JSONObject.class)
                            : new ArrayList<JSONObject>())
                    .orElseGet(ArrayList::new);
            if (StrUtil.isBlankIfStr(keyField)) {
                keyField = "name";
            }
            // 合并：现有配置中同名项保留先出现的（处理自身重复），再用新增配置覆盖同名项
            if (collector == null) {
                String finalKeyField = keyField;
                collector = Collectors.toMap(
                        j -> j.getStr(finalKeyField),
                        j -> j,
                        (first, second) -> first   // 自身冲突时保留第一个
                );
            }

            Map<String, JSONObject> mergedMap = existingList.stream()
                    .collect(collector);
            String finalKeyField1 = keyField;
            addList.forEach(item -> mergedMap.put(item.getStr(finalKeyField1), item));

            DbKV newKV = new DbKV();
            newKV.setType(kvKey);
            newKV.setKeyName(kvKey);
            newKV.setValue(JSONUtil.toJsonStr(mergedMap.values()));

            return dbKVService.saveOrUpdate(newKV, query);
        } catch (Exception e) {
            throw new RuntimeException("配置追加保存失败，key=" + kvKey, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCountryAll(String json) {
        DbKV dbKV = new DbKV();
        String id = KeyConstants.auto_plan_key_country_all;
        dbKV.setType(id);
        dbKV.setKeyName(id);
        dbKV.setValue(json);
        //
        //dbKVService.remove(
        //        Wrappers.lambdaQuery(DbKV.class)
        //                .eq(DbKV::getType, id)
        //                .eq(DbKV::getKeyName, id)
        //);

        return dbKVService.saveOrUpdate(dbKV, Wrappers.lambdaQuery(DbKV.class).eq(DbKV::getType, id).eq(DbKV::getKeyName, id));
        //return cacheService.save(KeyConstants.auto_plan_key_country_all, json);
    }

    @Override
    public List<String> findCountryAll() {
        LambdaQueryWrapper<DbKV> query = Wrappers.lambdaQuery(DbKV.class);
        query.eq(DbKV::getType, KeyConstants.auto_plan_key_country_all);

        DbKV dbKV = dbKVService.getOne(query);
        List<String> objectList = Optional.ofNullable(dbKV).map(k -> {
            String value = k.getValue();
            if (ObjectUtils.isEmpty(value)) {
                return new ArrayList<String>();
            }
            List<String> list = JSONUtil.toList(value, String.class);
            return list;
        }).orElse(new ArrayList<>());

        List<String> list = new ArrayList<>();
        if (CollUtil.isNotEmpty(objectList)) {
            list.addAll(objectList);
        }

        return list;
        //
        //Cache<String> cache = cacheService.find(KeyConstants.auto_plan_key_country_all);
        //return cache.toListByString();
    }
}
