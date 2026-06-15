package com.cloud_guest.runner;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.entitys.domain.Cache;
import com.cloud_guest.properties.load.LoadProperties;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.JSONUtils;
import com.cloud_guest.utils.LockUtil;
import com.cloud_guest.utils.LockYmlUtil;
import com.cloud_guest.utils.yml.YmlUtils;
import com.cloud_guest.wrappers.lock.LockWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2026/6/17 14:30:21
 * @Description
 */
@Slf4j
@Component
public class EncryptPasswordRunner {
    @Resource
    private LoadProperties loadProperties;
    @Resource
    private Environment env;
    @Resource
    private CacheService cacheService;

    public static String SALT = StrUtil.EMPTY;

    @PostConstruct
    public void init() {
        String key = KeyConstants.encrypt_salt;
        List<String> yamlPaths = loadProperties.getYamlPaths();

        LockWrapper lock = LockUtil.getLock(key, 800L, 0, TimeUnit.MILLISECONDS);
        if (!lock.tryLock()) {
            throw new RuntimeException("获取SALT初始化锁失败");
        }

        try {
            String cacheValue = cacheService.findValueByKey(key);
            String envValue = env.getProperty(key, StrUtil.EMPTY);

            SALT = determineSalt(envValue, cacheValue);

            log.info("EncryptPassword初始化完成，SALT:{}", SALT);

            cacheService.saveKeyValue(key, SALT);

            writeYamlFiles(yamlPaths, key, SALT);

        } finally {
            if (lock.isHeldByCurrentThread() && lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    private String determineSalt(String envValue, String cacheValue) {
        if (!StrUtil.isBlankIfStr(cacheValue)) {
            return cacheValue;
        }
        if (!StrUtil.isBlankIfStr(envValue)) {
            return envValue;
        }
        return StrUtil.uuid();
    }
    private void writeYamlFiles(List<String> yamlPaths, String key, String salt) {
        JSONObject saltJson = JSONUtils.toJSONObject(key, salt);

        yamlPaths.forEach(yamlPath -> {
            File file = FileUtil.newFile(yamlPath);
            if (file == null || !file.exists()) {
                log.warn("YAML文件不存在，跳过: {}", yamlPath);
                return;
            }

            try {
                String content = FileUtil.readUtf8String(file);
                JSONObject existingConfig;

                if (StrUtil.isBlankIfStr(content)) {
                    existingConfig = saltJson;
                } else {
                    existingConfig = YmlUtils.readValue(file, JSONObject.class);
                    if (existingConfig != null) {
                        existingConfig.putAll(saltJson);
                    } else {
                        existingConfig = saltJson;
                    }
                }

                LockYmlUtil.writeValue(file, existingConfig);
                log.debug("YAML文件写入成功: {}", yamlPath);
            } catch (Exception e) {
                log.error("YAML文件处理失败: {}", yamlPath, e);
            }
        });
    }

/*    private void writeYamlFiles(List<String> yamlPaths, String key, String salt) {
        JSONObject saltJson = JSONUtils.toJSONObject(key, salt);

        yamlPaths.forEach(yamlPath -> {
            File file = FileUtil.newFile(yamlPath);
            if (file == null || !file.exists()) {
                log.warn("YAML文件不存在，跳过: {}", yamlPath);
                return;
            }

            try {
                JSONObject existingConfig = YmlUtils.readValue(file, JSONObject.class);
                if (existingConfig != null) {
                    existingConfig.putAll(saltJson);
                } else {
                    JSONObject entries = JSONUtils.toJSONObject(key, salt);
                    existingConfig.putAll( entries);
                }
                LockYmlUtil.writeValue(file, existingConfig);
                log.debug("YAML文件写入成功: {}", yamlPath);
            } catch (Exception e) {
                log.error("YAML文件处理失败: {}", yamlPath, e);
            }
        });
    }*/


}
