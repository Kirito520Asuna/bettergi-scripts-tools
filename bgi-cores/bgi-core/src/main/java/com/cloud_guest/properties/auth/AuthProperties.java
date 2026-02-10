package com.cloud_guest.properties.auth;
import lombok.Data;
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
    private String tokenName= HttpHeaders.AUTHORIZATION;
    private List<User> users = new ArrayList<>();
    private Jwt jwt = new Jwt();

    @Data
    public static class User {
        private String username;
        private String password;
    }

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs;
    }
}