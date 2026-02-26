package com.cloud_guest.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.enums.OSType;
import com.cloud_guest.properties.load.LoadProperties;
import com.cloud_guest.service.ApplicationService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.bean.MapUtils;
import com.cloud_guest.utils.object.ObjectUtils;
import com.cloud_guest.utils.yml.YmlUtils;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        OSType currentOSType = OSType.getCurrentOSType();
        for (String yamlPath : yamlPaths) {
            //File pathFile = new File(yamlPath);
            OSType osType = OSType.detectByPathFormat(yamlPath);
            if (ObjectUtils.equals(OSType.UNKNOWN, osType)) {
                log.debug("{}是相对路径", yamlPath);
            } else if (OSType.isUnixLike(osType)) {
                if (!OSType.isUnixLike(null)) {
                    //地址 linux ,系统非linux
                    log.warn("[跳过写入][{}]{}是{}绝对路径", currentOSType, yamlPath, "[非类unix系统]");
                    continue;
                }
            } else if (!OSType.isUnixLike(null)) {
                if (OSType.isUnixLike(osType)) {
                    //地址 非linux ,系统linux
                    log.warn("[跳过写入][{}]{}是{}绝对路径", currentOSType, yamlPath, "[非类unix系统]");
                    continue;
                }
            }

            try {
                JSONObject jsonObject = YmlUtils.readValueToJSONObject(yamlPath);
                //if (jsonObject == null) {
                //    continue;
                //}
                File file = FileUtil.newFile(yamlPath);

                if (file == null || !file.exists()) {
                    continue;
                }
                jsonObject = setCheckToken(name, value, jsonObject);
                YmlUtils.writeValue(file, jsonObject);
            }catch (MismatchedInputException e){
                log.warn("{}文件格式不正确/文件为空", yamlPath);
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
    public JSONObject setCheckToken(String name, String value, JSONObject jsonObject) {

        String checkName = "check";
        String tokenName = "token";
        String nameKey = "name";
        String valueKey = "value";
        Map<String, Object> map = MapUtils.createHierarchicalMap(checkName + "." + tokenName);
        JSONObject checkToken = new JSONObject();
        checkToken.putAll(map);
        // 获取或创建 check 对象
        JSONObject check = (JSONObject) checkToken.getByPath(checkName);
        if (check == null) {
            check = new JSONObject();
            checkToken.put(checkName, check);
        }

        // 获取或创建 token 对象（checkName → tokenName）
        JSONObject token = (JSONObject) check.get(tokenName);
        if (token == null) {
            token = new JSONObject();
            check.put(tokenName, token);
        }

        // 获取或创建 tokenValue（最里层那个放 name 和 value 的对象）
        JSONObject tokenValue = (JSONObject) token.get(tokenName);
        if (tokenValue == null) {
            tokenValue = new JSONObject();
            //token.put(tokenName, tokenValue);
        }

        // 直接设置值（会覆盖旧值，这通常是想要的行为）
        tokenValue.put(nameKey, StrUtil.isNotBlank(name) ? name : "");
        tokenValue.put(valueKey, StrUtil.isNotBlank(value) ? value : "");
        token.putAll(tokenValue);

        jsonObject.putAll(checkToken);

        saveLoadApplicationYml(jsonObject);
        return jsonObject;
    }
}
