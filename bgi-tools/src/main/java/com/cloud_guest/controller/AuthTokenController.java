package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Login;
import com.cloud_guest.properties.check.TokenProperties;
import com.cloud_guest.result.Result;
import com.cloud_guest.vo.TokenVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    private TokenProperties tokenProperties;
    @Login
    @SysLog
    @Operation(summary = "[需要登录]获取授权token")
    @GetMapping("info")
    public Result<TokenVo> token() {
        String tokenName = tokenProperties.getName();
        String tokenValue = tokenProperties.getValue();
        TokenVo tokenVo = new TokenVo();
        tokenVo.setName(tokenName);
        tokenVo.setValue(tokenValue);
        return ok(tokenVo);
    }
}
