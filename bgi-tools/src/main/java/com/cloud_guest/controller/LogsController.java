package com.cloud_guest.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Login;
import com.cloud_guest.domain.LogKey;
import com.cloud_guest.pojo.DbKV;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.service.DbKVService;
import com.cloud_guest.service.LogsService;
import com.cloud_guest.utils.ApplicationUtil;
import com.cloud_guest.utils.IdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yan
 * @Date 2026/5/7 16:48:27
 * @Description
 */
@Slf4j
@Tag(name = "日志")
@RestController
@RequestMapping(value = {"/jwt/log/"})
public class LogsController {
    @Resource
    private LogsService logsService;


    @SysLog
    @Operation(summary = "日志文件列表")
    @GetMapping("file-names")
    public Result<Map<String, Object>> fileNames(@RequestParam String applicationId) {
        String currentApplicationId = ApplicationUtil.getApplicationId();

        Map<String, Object> result = null;

        if(StrUtil.equals(applicationId, currentApplicationId)){
            List<String> list = logsService.getFileNames();
            result = new HashMap<>();
            result.put("applicationId", currentApplicationId);
            result.put("fileNames", list);
        }

        return Result.ok(result);
    }

    @SysLog
    @Login
    @Operation(summary = "日志授权token")
    @GetMapping("auth-token")
    public Result<LogKey> authToken() {
        LogKey logKey = logsService.createLogKey();
        return Result.ok(logKey);
    }

}
