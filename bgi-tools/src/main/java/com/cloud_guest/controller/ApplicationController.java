package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Login;
import com.cloud_guest.domain.dto.ApplicationDto;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.ApplicationUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/12 21:25:06
 * @Description
 */
@RestController
@RequestMapping(value = {"/api/application/", "/jwt/application/"})
public class ApplicationController {
    @Resource
    private RestartEndpoint restartEndpoint;

    @Login
    @SysLog
    @Operation(summary = "[需要登录]重启")
    @PostMapping("restart")
    public Result restart(@Validated @RequestBody ApplicationDto dto) {
        List<String> ids = dto.getIds();
        String applicationId = ApplicationUtil.getApplicationId();
        if (ids.contains(applicationId)) {
            restartEndpoint.restart();
        } else {
            applicationId = null;
        }
        return Result.ok(applicationId);
    }

    @SysLog
    @Operation(summary = "判断重启")
    @GetMapping("info")
    public Result info() {
        return Result.ok();
    }
}
