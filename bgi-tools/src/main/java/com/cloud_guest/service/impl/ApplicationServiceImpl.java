package com.cloud_guest.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.cloud_guest.properties.load.LoadProperties;
import com.cloud_guest.service.ApplicationService;
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
    private LoadProperties loadProperties;

    @SneakyThrows
    @Override
    public boolean saveToken(String name, String value) {
        List<String> yamlPaths = loadProperties.getYamlPaths();

        for (String yamlPath : yamlPaths) {
            try {
                File file = FileUtil.newFile(yamlPath);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject = YmlUtils.readValue(file, JSONObject.class);
                } catch (MismatchedInputException e) {
                    log.warn("{}", e.getMessage());
                }
                setSysToken(name, value, jsonObject);
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
        return check;
    }
}
