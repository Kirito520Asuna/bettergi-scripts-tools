package com.cloud_guest.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.pojo.DbKV;
import com.cloud_guest.properties.load.LoadProperties;
import com.cloud_guest.service.ApplicationService;
import com.cloud_guest.service.DbKVService;
import com.cloud_guest.utils.ApplicationUtil;
import com.cloud_guest.utils.LockUtil;
import com.cloud_guest.utils.LockYmlUtil;
import com.cloud_guest.utils.bean.MapUtils;
import com.cloud_guest.utils.object.ObjectUtils;
import com.cloud_guest.utils.yml.YmlUtils;
import com.cloud_guest.wrappers.lock.LockWrapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/23 16:11:31
 * @Description
 */
@Slf4j
@Service
public class ApplicationServiceImpl implements ApplicationService {
    @Resource
    private DbKVService dbKVService;

    @Resource
    private LoadProperties loadProperties;

    private static final String CHECK_NAME = "check";
    private static final String TOKEN_NAME = "token";
    private static final String NAME_KEY = "name";
    private static final String VALUE_KEY = "value";

    @SneakyThrows
    @Override
    public boolean saveToken(String name, String value) {
        List<String> yamlPaths = loadProperties.getYamlPaths();
        String loadYmlSaveUpdateTimeKey = KeyConstants.load_yml_save_update_time_key;

        for (String yamlPath : yamlPaths) {
            try {
                JSONObject jsonObject = readAndValidateYaml(yamlPath);
                if (jsonObject == null) {
                    continue;
                }

                jsonObject = setCheckToken(name, value, jsonObject);
                String lockKey = KeyConstants.load_yml_write_key + ":" + yamlPath;
                LockWrapper lock = LockUtil.getLock(lockKey);

                saveLoadApplicationYml(jsonObject);
                jsonObject.remove(loadYmlSaveUpdateTimeKey);
                LockYmlUtil.writeValue(FileUtil.newFile(yamlPath), jsonObject, lock);

            } catch (MismatchedInputException e) {
                log.warn("{}文件格式不正确/文件为空", yamlPath);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("文件不存在或为空")) {
                    log.debug("文件不存在或为空: {}", yamlPath);
                    continue;
                }
                log.error("保存token失败, yamlPath: {}", yamlPath, e);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean loadApplicationYml(Long loadTime) {
        JSONObject jsonObject = loadLatestDbKV();
        if (jsonObject == null) {
            return false;
        }

        String loadYmlSaveUpdateTimeKey = KeyConstants.load_yml_save_update_time_key;
        Long updateTime = jsonObject.getLong(loadYmlSaveUpdateTimeKey);

        if (updateTime != null && loadTime != null) {
            long elapsed = System.currentTimeMillis() - updateTime;
            if (elapsed > loadTime) {
                return false;
            }
        }

        jsonObject.remove(loadYmlSaveUpdateTimeKey);
        List<String> yamlPaths = loadProperties.getYamlPaths();

        if (CollUtil.isNotEmpty(yamlPaths)) {
            log.debug("加载{}ms *.yml", loadTime == null ? 0 : loadTime);
        }

        return writeToAllYamlPaths(jsonObject, yamlPaths);
    }
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public boolean saveLoadApplicationYml(JSONObject jsonObject) {
        if (jsonObject == null) {
            return false;
        }

        String loadYmlSaveUpdateTimeKey = KeyConstants.load_yml_save_update_time_key;
        jsonObject.put(loadYmlSaveUpdateTimeKey, System.currentTimeMillis());

        LockWrapper lock = LockUtil.getLock(KeyConstants.load_yml_save_key);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            log.warn("获取锁超时，key: {}", KeyConstants.load_yml_save_key);
            throw new GlobalException("存在其他操作，请稍后再试!");
        }

        try {
            // 先移除同类型的数据
            dbKVService.remove(
                    Wrappers.lambdaQuery(DbKV.class)
                            .eq(DbKV::getType, KeyConstants.load_yml_save_key)
            );
            log.debug("已清除类型为 {} 的历史数据", KeyConstants.load_yml_save_key);

            DbKV kv = new DbKV();
            kv.setType(KeyConstants.load_yml_save_key);
            kv.setKey(KeyConstants.load_yml_save_key);
            kv.setValue(JSONUtil.toJsonStr(jsonObject));
            dbKVService.save(kv);
        } finally {
            safeUnlock(lock, tryLock, KeyConstants.load_yml_save_key);
        }
        return true;
    }

    @Override
    public JSONObject setCheckToken(String name, String value, JSONObject jsonObject) {
        JSONObject check = jsonObject.getJSONObject(CHECK_NAME);
        if (check == null) {
            check = new JSONObject();
            jsonObject.put(CHECK_NAME, check);
        }

        JSONObject token = check.getJSONObject(TOKEN_NAME);
        if (token == null) {
            token = new JSONObject();
            check.put(TOKEN_NAME, token);
        }

        token.put(NAME_KEY, StrUtil.blankToDefault(name, ""));
        token.put(VALUE_KEY, StrUtil.blankToDefault(value, ""));

        jsonObject.put(KeyConstants.load_yml_save_update_time_key, System.currentTimeMillis());
        return jsonObject;
    }

    /**
     * 读取并验证YAML文件
     */
    @SneakyThrows
    private JSONObject readAndValidateYaml(String yamlPath) {
        File file = FileUtil.newFile(yamlPath);
        if (file == null || !file.exists()) {
            log.debug("文件不存在: {}", yamlPath);
            return null;
        }
        return YmlUtils.readValueToJSONObject(yamlPath);
    }

    /**
     * 从数据库加载最新的配置
     */
    private JSONObject loadLatestDbKV() {
        List<DbKV> kvs = dbKVService.list(
                Wrappers.lambdaQuery(DbKV.class)
                        .select()
                        .eq(DbKV::getType, KeyConstants.load_yml_save_key)
                        .orderByDesc(DbKV::getUpdateTime)
        );

        DbKV dbKV = kvs.stream().findFirst().orElse(null);
        if (dbKV == null || StrUtil.isBlank(dbKV.getValue())) {
            return null;
        }

        return JSONUtil.toBean(dbKV.getValue(), JSONObject.class);
    }

    /**
     * 写入所有YAML路径
     */
    private boolean writeToAllYamlPaths(JSONObject jsonObject, List<String> yamlPaths) {
        for (String yamlPath : yamlPaths) {
            try {
                File file = FileUtil.newFile(yamlPath);
                if (jsonObject != null) {
                    String lockKey = KeyConstants.load_yml_write_key + ":" + yamlPath;
                    LockWrapper lock = LockUtil.getLock(lockKey);
                    LockYmlUtil.writeValue(file, jsonObject, lock);
                    log.debug("应用{}加载{}完成", ApplicationUtil.getApplicationId(), yamlPath);
                }
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("文件不存在或为空")) {
                    log.debug("文件不存在或为空: {}", yamlPath);
                    continue;
                }
                log.error("写入YAML失败, yamlPath: {}", yamlPath, e);
                return false;
            }
        }
        return true;
    }

    /**
     * 安全释放锁
     */
    private void safeUnlock(LockWrapper lock, boolean tryLock, String lockKey) {
        if (tryLock) {
            try {
                lock.unlock();
                log.debug("锁释放成功: {}", lockKey);
            } catch (Exception e) {
                log.error("锁释放失败: {}", lockKey, e);
            }
        }
    }
}