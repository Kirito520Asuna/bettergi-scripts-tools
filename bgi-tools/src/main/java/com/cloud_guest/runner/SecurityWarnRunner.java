package com.cloud_guest.runner;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.properties.auth.AuthProperties;
import com.cloud_guest.properties.check.TokenProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @Author yan
 * @Date 2026/6/18 10:00:00
 * @Description 启动安全基线检查：对不安全的默认配置在启动完成时输出告警
 */
@Slf4j
@Component
public class SecurityWarnRunner implements ApplicationRunner {

    /** application.yml 中的默认账号，检测到仍在使用时输出高危告警 */
    private static final String DEFAULT_USERNAME = "bgi_tools";
    private static final String DEFAULT_PASSWORD = "bgi_tools";

    @Resource
    private AuthProperties authProperties;
    @Resource
    private TokenProperties tokenProperties;

    @Override
    public void run(ApplicationArguments args) {
        warnDefaultCredentials();
        warnTokenNotConfigured();
        warnDefaultJwtSecret();
    }

    /** 默认弱口令告警 */
    private void warnDefaultCredentials() {
        boolean usingDefault = authProperties.getUsers().stream()
                .anyMatch(u -> DEFAULT_USERNAME.equals(u.getUsername()) && DEFAULT_PASSWORD.equals(u.getPassword()));
        if (usingDefault) {
            log.error("【安全告警】管理账号仍在使用默认弱口令 bgi_tools/bgi_tools，任何能访问本服务的人都可登录管理界面获取 JWT！请立即通过【认证模块-修改账户】修改账号密码并重启。");
        }
    }

    /** 未配置 check.token 的开放接口提示 */
    private void warnTokenNotConfigured() {
        if (StrUtil.isBlank(tokenProperties.getName()) || StrUtil.isBlank(tokenProperties.getValue())) {
            log.warn("【安全提示】未配置 check.token，/bgi/ 前缀接口（OCR、Cron、秘境计划等）处于无鉴权开放状态（本地部署的默认设计）。若服务暴露于公网/局域网，请配置 check.token 或在网关层增加鉴权！");
        }
    }

    /** JWT 密钥使用代码内置默认值的告警 */
    private void warnDefaultJwtSecret() {
        AuthProperties.Jwt jwt = authProperties.getJwt();
        if (jwt != null && StrUtil.equals(new AuthProperties.Jwt().DEFAULT_SECRET, jwt.getSecret())) {
            log.warn("【安全提示】JWT 密钥使用代码内置默认值，拿到密钥的人可以伪造任意登录令牌，请在配置文件中修改 auth.jwt.secret！");
        }
    }
}
