package com.cloud_guest.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.properties.load.LoadProperties;
import com.cloud_guest.service.ApplicationService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.yml.YmlUtils;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    private CacheService cacheService;

    @Resource
    private LoadProperties loadProperties;

    @SneakyThrows
    @Override
    public boolean saveToken(String name, String value) {
        List<String> yamlPaths = loadProperties.getYamlPaths();

        for (String yamlPath : yamlPaths) {
            try {
                JSONObject jsonObject = YmlUtils.readValueToJSONObject(yamlPath);
                //if (jsonObject == null) {
                //    continue;
                //}
                File file = FileUtil.newFile(yamlPath);

                if (file == null || !file.exists()) {
                    continue;
                }
                jsonObject = setSysToken(name, value, jsonObject);
                YmlUtils.writeValue(file, jsonObject);
            } catch (Exception e) {
                if (e.getMessage().contains("文件不存在或为空")) {
                    continue;
                } else {
                    return false;
                }
            }
        }

        return true;
    }
    @Override
    public boolean loadApplicationYml() {
        JSONObject jsonObject = null;
        //todo: 远程/本地存储加载
        Cache<String> cache = cacheService.find(KeyConstants.load_yml_key);
        if (cache != null) {
            String data = cache.getData();
            if (StrUtil.isNotBlank(data)) {
                jsonObject = JSONUtil.toBean(data, JSONObject.class);
            }
        }

        List<String> yamlPaths = loadProperties.getYamlPaths();
        for (String yamlPath : yamlPaths) {
            try {
                File file = FileUtil.newFile(yamlPath);
                if (jsonObject != null) {
                    YmlUtils.writeValue(file, jsonObject);
                }
            } catch (Exception e) {
                if (e.getMessage().contains("文件不存在或为空")) {
                    continue;
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public boolean saveLoadApplicationYml(JSONObject jsonObject) {
        if (jsonObject == null) {
            return false;
        }
        cacheService.save(KeyConstants.load_yml_key, JSONUtil.toJsonStr(jsonObject));
        return true;
    }

    @Override
    public JSONObject setSysToken(String name, String value, JSONObject jsonObject) {
        String checkName = "check";
        String tokenName = "token";
        String nameKey = "name";
        String valueKey = "value";
        JSONObject check = (JSONObject) jsonObject.getByPath(checkName);
        if (check == null) {
            JSONObject token = new JSONObject();
            JSONObject tokenValue = new JSONObject();

            tokenValue.put(nameKey, name);
            tokenValue.put(valueKey, value);

            token.put(tokenName, tokenValue);
            jsonObject.put(checkName, token);
            check = (JSONObject) jsonObject.getByPath(checkName);
            jsonObject.put(checkName, check);
        }
        JSONObject token = (JSONObject) jsonObject.getByPath(checkName + "." + tokenName);
        if (token == null) {
            token = new JSONObject();
            JSONObject tokenValue = new JSONObject();

            tokenValue.put(nameKey, name);
            tokenValue.put(valueKey, value);

            token.put(tokenName, tokenValue);
            jsonObject.put(checkName, token);

            token = (JSONObject) jsonObject.getByPath(checkName + "." + tokenName);
        }
        token.put(nameKey, name);
        token.put(valueKey, value);
        saveLoadApplicationYml(jsonObject);
        return check;
    }
}
