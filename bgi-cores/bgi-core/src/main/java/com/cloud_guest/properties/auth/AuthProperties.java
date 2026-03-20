package com.cloud_guest.properties.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/10 12:51:34
 * @Description
 */
@Component
@ConfigurationProperties(prefix = "auth")
@Data
public class AuthProperties {
    private boolean enabled = false;
    private String tokenName = HttpHeaders.AUTHORIZATION;
    private List<User> users = new ArrayList<>();
    private Jwt jwt = new Jwt();
    private Ttl ttl = new Ttl();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Jwt {
        public static final long JWT_TTL = 24 * 60 * 60 * 1000L;// 60 * 60 *1000  一个小时
        //public static final long LONG_JWT_TTL = 30 * JWT_TTL;
        private String secret = "a2lyaXRvYXN1bkeyMbgi/toolskwMTIzNDjwtzg5MDEy";
        private long expirationMs = JWT_TTL;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ttl {
        //过期时间
        private long expirationMs = Jwt.JWT_TTL * 7;
        //刷新间隔
        private long refreshIntervalExpirationMs = Jwt.JWT_TTL / 24;
    }
}