package com.cloud_guest.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.ApplicationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
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
    @Value("${logging.file.path:./logs}")
    private String LOG_PATH;

    private String LOG_NAME;

    @PostConstruct
    public void init() {
        Environment bean = SpringUtil.getBean(Environment.class);
        String logName = bean.getProperty("logging.file.name", "bgi-tools.log");
        if (!logName.endsWith(".log")) {
            logName = logName + ".log";
        }
        // 去除文件名中的路径
        LOG_NAME = new File(logName).getName();

        log.info("日志目录: {}", LOG_PATH);
        log.info("日志文件: {}", LOG_NAME);
    }

    @SysLog
    @Operation(summary = "日志文件列表")
    @GetMapping("file-names")
    public Result<Map<String, Object>> fileNames(@RequestParam String applicationId) {
        String currentApplicationId = ApplicationUtil.getApplicationId();

        Map<String, Object> result = null;

        if(StrUtil.equals(applicationId, currentApplicationId)){
            List<String> list = new ArrayList<>();
            File logDir = new File(LOG_PATH);

            if (!logDir.exists() || !logDir.isDirectory()) {
                log.warn("日志目录不存在: {}", LOG_PATH);
            } else {
                File[] files = logDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.isDirectory()) {
                            list.add(file.getName());
                        }
                    }
                }
            }
            //LOG_NAME 排第一个 其他按照时间倒序

            list.sort((a, b) -> {
                if (a.equals(LOG_NAME)) return -1;
                if (b.equals(LOG_NAME)) return 1;

                long timeA = new File(LOG_PATH, a).lastModified();
                long timeB = new File(LOG_PATH, b).lastModified();
                return Long.compare(timeB, timeA);
            });

            result = new HashMap<>();
            result.put("applicationId", currentApplicationId);
            result.put("fileNames", list);
        }

        return Result.ok(result);
    }
}
