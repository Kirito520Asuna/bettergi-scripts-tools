package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Login;
import com.cloud_guest.entitys.dto.CheckTokenDto;
import com.cloud_guest.properties.check.TokenProperties;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.ApplicationService;
import com.cloud_guest.entitys.records.TokenVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import static com.cloud_guest.result.Result.ok;

/**
 * @Author yan
 * @Date 2026/2/10 13:35:31
 * @Description
 */
@Tag(name = "授权token服务")
@RestController
@RequestMapping(value = {"/jwt/token/"})
public class AuthTokenController {
    @Resource
    private ApplicationService applicationService;
    @Resource
    private TokenProperties tokenProperties;
    @Login
    @SysLog
    @Operation(summary = "[需要登录]获取授权token")
    @GetMapping("info")
    public Result<TokenVo> token() {
        String tokenName = tokenProperties.getName();
        String tokenValue = tokenProperties.getValue();
        return ok(new TokenVo(tokenName, tokenValue));
    }

    @Login
    @SysLog
    @Operation(summary = "[需要登录]设置授权token")
    @PostMapping("info")
    public Result tokenSave(@Validated @RequestBody CheckTokenDto token) {
        String tokenName = token.getTokenName();
        String tokenValue = token.getTokenValue();
        return ok(applicationService.saveToken(tokenName, tokenValue));
    }
}
