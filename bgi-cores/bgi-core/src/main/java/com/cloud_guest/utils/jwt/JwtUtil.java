package com.cloud_guest.utils.jwt;

/**
 * @Author yan
 * @Date 2026/2/10 12:49:33
 * @Description
 */
import com.cloud_guest.properties.auth.AuthProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Autowired
    private AuthProperties authProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(authProperties.getJwt().getSecret().getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + authProperties.getJwt().getExpirationMs()))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
