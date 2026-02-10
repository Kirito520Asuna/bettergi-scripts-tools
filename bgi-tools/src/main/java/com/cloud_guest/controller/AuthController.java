package com.cloud_guest.controller;

import com.cloud_guest.domain.LoginDto;
import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.properties.auth.AuthProperties;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    private AuthProperties authProperties;
    @Resource
    private JwtUtil jwtUtil;
    @PostMapping("login")
    public Result<?> login(@Validated @RequestBody LoginDto dto) {
        //String username = body.get("username");
        //String password = body.get("password");
        //
        //if (username == null || password == null) {
        //    return ResponseEntity.badRequest().body(Map.of("error", "缺少用户名或密码"));
        //}
        String username = dto.getUsername();
        String password = dto.getPassword();
        boolean matched = authProperties.getUsers().stream()
                .anyMatch(u -> u.getUsername().equals(username) && u.getPassword().equals(password));

        if (!matched) {
            ApiCode fail = ApiCode.LOGIN_FAIL;
            throw new GlobalException(fail.getCode(), fail.getMessage());
        }

        String token = jwtUtil.generateToken(username);
        return Result.ok(token);
    }
}

