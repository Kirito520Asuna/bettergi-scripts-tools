package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Login;
import com.cloud_guest.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author yan
 * @Date 2026/2/12 21:25:06
 * @Description
 */
@RestController
@RequestMapping(value = {"/api/application/","/jwt/application/"})
public class ApplicationController {
    @Resource
    private RestartEndpoint restartEndpoint;
    @Login
    @SysLog
    @Operation(summary = "[需要登录]重启")
    @PostMapping("restart")
    public Result restart(){
        restartEndpoint.restart();
        return Result.ok();
    }

    @SysLog
    @Operation(summary = "判断重启")
    @GetMapping("info")
    public Result info(){
        return Result.ok();
    }
}
