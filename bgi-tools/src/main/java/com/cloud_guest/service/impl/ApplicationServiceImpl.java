package com.cloud_guest.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.cloud_guest.properties.load.LoadProperties;
import com.cloud_guest.service.ApplicationService;
import com.cloud_guest.utils.yml.YmlUtils;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/23 16:11:31
 * @Description
 */
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
                JSONObject jsonObject = YmlUtils.readValue(file, JSONObject.class);
                JSONObject byPath = (JSONObject) jsonObject.getByPath("check.token");
                if (byPath == null) {
                    byPath = new JSONObject();
                }
                byPath.put("name", name);
                byPath.put("value", value);
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
}
