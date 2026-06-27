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

import java.util.*;
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

    // 1. 领域数据（JSONObject 列表，按 "name" 字段合并）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDomainAllByAdd(String json) {
        return addConfigsToDbKV(
                KeyConstants.auto_plan_key_domain_all,
                "name",
                json,
                null,                                    // 使用默认历史数据冲突策略
                JSONObject.class,
                j -> j.getStr("name")                    // 类型安全的键提取
        );
    }

    // 2. 国家列表（纯字符串数组，键为字符串本身）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCountryAllByAdd(String json) {
        return addConfigsToDbKV(
                KeyConstants.auto_plan_key_country_all,
                null,                                    // 无需字段反射，直接使用整体作为键
                json,
                null,
                String.class,
                Function.identity()                       // 字符串身份作为键
        );
    }

    // 3. BOSS 数据（JSONObject 列表，按 "name" 字段合并，使用默认内部冲突规则）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBossAllByAdd(String json) {
        return addConfigsToDbKV(
                KeyConstants.auto_plan_key_boss_all,
                "name",
                json,
                null,                                    // 使用默认历史数据冲突策略
                JSONObject.class,
                j -> j.getStr("name")
        );
    }

    /**
     * 向数据库键值配置表追加/合并配置。
     * <p>
     * 合并规则：现有配置中的同名项保留先出现的；新增配置中的同名项保留后出现的（后覆盖前），
     * 最终用新增项覆盖现有项中同名的记录。
     *
     * @param kvKey    配置键（type与keyName共用）
     * @param keyField 作为合并依据的字段名，为空时默认使用 "name"
     * @param json     待新增的 JSON 数组字符串
     * @param collector 自定义合并收集器，为 null 时使用默认规则（现有个保留先出现的）
     * @param clazz    目标实体类型
     * @param keyExtractor 从实体中提取键的函数（替代反射调用 getStr，提供类型安全）
     * @param <T>      实体类型
     * @return 保存成功返回 true，json 为空或解析后列表为空返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> boolean addConfigsToDbKV(String kvKey, String keyField, String json,
                                        Collector<T, ?, Map<String, T>> collector,
                                        Class<T> clazz,
                                        Function<T, String> keyExtractor) {

        // 1. 空值快速失败
        if (StrUtil.isBlank(json)) {
            log.debug("addConfigsToDbKV: json 为空，跳过处理，key="+kvKey);
            return false;
        }

        try {
            // 2. 解析新增列表
            List<T> addList = JSONUtil.toList(json, clazz);
            if (CollUtil.isEmpty(addList)) {
                log.warn("addConfigsToDbKV: 解析后新增列表为空，key="+kvKey);
                return false;
            }

            // 3. 确定键字段名，并构建类型安全的键提取器
            if (StrUtil.isBlank(keyField)) {
                keyField = "name";
            }
            String finalKeyField = keyField;

            // 4. 新建有序 Map，保证多次保存的 JSON 数组顺序一致
            Map<String, T> mergedMap = new LinkedHashMap<>();

            // 5. 处理历史数据
            LambdaQueryWrapper<DbKV> query = Wrappers.lambdaQuery(DbKV.class)
                    .eq(DbKV::getType, kvKey)
                    .eq(DbKV::getKeyName, kvKey);
            DbKV existingKV = dbKVService.getOne(query);

            if (existingKV != null && StrUtil.isNotBlank(existingKV.getValue())) {
                List<T> existingList = JSONUtil.toList(existingKV.getValue(), clazz);
                if (CollUtil.isNotEmpty(existingList)) {
                    // 使用默认或自定义合并策略先处理历史数据内部重复
                    Collector<T, ?, Map<String, T>> existingCollector = (collector != null)
                            ? collector
                            : Collectors.toMap(keyExtractor, Function.identity(), (first, second) -> first);
                    Map<String, T> existingMap = existingList.stream().collect(existingCollector);
                    mergedMap.putAll(existingMap); // 保留现有配置
                }
            }

            // 6. 合并新增数据（新值覆盖旧值，新增内部后出现的覆盖先出现的）
            //    使用 LinkedHashMap 保持添加顺序
            Map<String, T> addMap = addList.stream()
                    .collect(Collectors.toMap(keyExtractor, Function.identity(),
                            (oldInAdd, newInAdd) -> newInAdd, // 新增内部冲突：保留后出现的
                            LinkedHashMap::new));
            mergedMap.putAll(addMap); // 后 put 会覆盖同名字段

            // 7. 保存（values 顺序确定）
            DbKV newKV = new DbKV();
            newKV.setType(kvKey);
            newKV.setKeyName(kvKey);
            newKV.setValue(JSONUtil.toJsonStr(new ArrayList<>(mergedMap.values())));

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
