package com.cloud_guest.properties.check;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * @Author yan
 * @Date 2026/2/10 13:45:36
 * @Description
 */
@Component
@ConfigurationProperties(prefix = "check.token")
@Data
public class TokenProperties {
    private String name = HttpHeaders.AUTHORIZATION;
    private String value = StrUtil.EMPTY;
}
