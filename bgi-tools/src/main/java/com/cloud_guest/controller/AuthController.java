package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Token;
import com.cloud_guest.entitys.dto.LoginDto;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.AuthService;
import com.cloud_guest.utils.AuthContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @Author yan
 * @Date 2026/2/10 14:36:49
 * @Description
 */
@Tag(name = "认证模块")
@RestController
@RequestMapping("/auth/")
public class AuthController {
    @Resource
    private AuthService authService;
    @SysLog
    @Operation(summary = "登录")
    @PostMapping("login")
    public Result<?> login(@Validated @RequestBody LoginDto dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();
        String token = authService.login(username, password);
        return Result.ok(token);
    }
    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权]修改账户(重启生效)")
    @PostMapping("info")
    public Result info(@Validated @RequestBody LoginDto dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();
        authService.saveUser(username, password);
        return Result.ok();
    }
    @SysLog
    @Token
    @Operation(summary = "获取当前用户名")
    @GetMapping("info/username")
    public Result<String> info() {
        String username = AuthContextUtil.getUsernameNoThrow();
        return Result.ok(username);
    }
}

