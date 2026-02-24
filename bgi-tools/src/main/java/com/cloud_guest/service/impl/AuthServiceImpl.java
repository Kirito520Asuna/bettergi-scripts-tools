package com.cloud_guest.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.properties.auth.AuthProperties;
import com.cloud_guest.properties.load.LoadProperties;
import com.cloud_guest.service.AuthService;
import com.cloud_guest.utils.jwt.JwtUtil;
import com.cloud_guest.utils.yml.YmlUtils;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/24 20:38:04
 * @Description
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    @Resource
    private LoadProperties loadProperties;
    @Resource
    private AuthProperties authProperties;
    @Resource
    private JwtUtil jwtUtil;

    @Override
    public String login(String username, String password) {
        boolean matched = authProperties.getUsers().stream()
                .anyMatch(u -> u.getUsername().equals(username) && u.getPassword().equals(password));

        if (!matched) {
            ApiCode fail = ApiCode.LOGIN_FAIL;
            throw new GlobalException(fail.getCode(), fail.getMessage());
        }

        String token = jwtUtil.generateToken(username);
        return token;
    }

    @SneakyThrows
    @Override
    public boolean saveUser(String username, String password) {
        List<String> yamlPaths = loadProperties.getYamlPaths();
        String authKey = "auth";
        String usersKey = "users";
        //String authUsersKey = authKey + "." + usersKey;

        for (String yamlPath : yamlPaths) {
            JSONObject jsonObject = YmlUtils.readValueToJSONObject(yamlPath);
            //if (jsonObject == null) {
            //    continue;
            //}
            File file = FileUtil.newFile(yamlPath);
            if (file == null || !file.exists()) {
                continue;
            }
            JSONObject auth = (JSONObject) jsonObject.getByPath(authKey);
            if (auth == null || true) {
                auth = new JSONObject();
            }
            JSONObject users = new JSONObject();

            ArrayList arrayList = new ArrayList();

            AuthProperties.User value = new AuthProperties.User(username, password);
            arrayList.add(value);
            String jsonStr = JSONUtil.toJsonStr(arrayList);


            JSONArray jsonArray = JSONUtil.parseArray(jsonStr);
            users.put(usersKey, jsonArray);
            auth.put(authKey, users);
            jsonObject.putAll(auth);
            YmlUtils.writeValue(file, jsonObject);
        }

        return true;
    }
}
