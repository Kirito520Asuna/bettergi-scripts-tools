package com.cloud_guest.utils.jwt;

/**
 * @Author yan
 * @Date 2026/2/10 12:49:33
 * @Description
 */

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.exception.exceptions.GlobalCustomException;
import com.cloud_guest.properties.auth.AuthProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@Data
public class JwtUtil {
    private long expire;
    private long expireLong;
    private String secret;
    private String header = HttpHeaders.AUTHORIZATION;
    private String isSuer = "bgi-tools";
    @Resource
    private AuthProperties authProperties;

    @PostConstruct
    public void init() {
        AuthProperties.Jwt jwt = authProperties.getJwt();
        secret = jwt.getSecret();
        expire = jwt.getExpirationMs();
        expireLong = expire * 30;

    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(authProperties.getJwt().getSecret().getBytes());
    }

    public String generateToken(String username) {
        return createJWT(username);
    }

    public String getUsernameFromToken(String token) {
        try {
            return getSubjectByParseJWT(token);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        return isTokenExpired(token);
    }

    public static JwtUtil fetchJwtUtils() {
        JwtUtil jwtUtils = new JwtUtil();
        try {
            jwtUtils = SpringUtil.getBean(JwtUtil.class);
        } catch (Exception e) {
            log.warn("未找到JwtUtils Bean");
        }
        return jwtUtils;
    }

    public static long getJWT_TTL() {
        return fetchJwtUtils().getExpire();
    }

    public static long getLONG_JWT_TTL() {
        return fetchJwtUtils().getExpireLong();
    }

    public static String getJWT_KEY() {
        return fetchJwtUtils().getSecret();
    }

    public static String getHEADER_AS_TOKEN() {
        return fetchJwtUtils().getHeader();
    }

    public static String getIS_SUER() {
        return fetchJwtUtils().getIsSuer();
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * @param subject
     * @return
     */
    public static String createJWT(String subject) {
        long JWT_TTL = getJWT_TTL();
        return createJWT(subject, JWT_TTL);
    }

    public static String createJWT(String subject, Long ttlMillis) {
        return createJWT(subject, ttlMillis, getJWT_KEY(), getIS_SUER());
    }

    /**
     * 生成jtw
     *
     * @param subject token中要存放的数据（json格式）
     * @return
     */
    public static String createJWT(String subject, String secret, String issuer) {
        JwtBuilder builder = getJwtBuilder(subject, null, getUUID(), secret, issuer);// 设置过期时间
        return builder.compact();
    }


    /**
     * 生成jtw
     *
     * @param subject   token中要存放的数据（json格式）
     * @param ttlMillis token超时时间
     * @return
     */
    public static String createJWT(String subject, Long ttlMillis, String secret, String issuer) {
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, getUUID(), secret, issuer);// 设置过期时间
        return builder.compact();
    }

    private static JwtBuilder getJwtBuilder(String subject, Long ttlMillis, String uuid, String secret, String issuer) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = generalKey(secret);
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if (ttlMillis == null) {
            ttlMillis = getJWT_TTL();
        }
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        return Jwts.builder()
                .setId(uuid)              //唯一的ID
                .setSubject(subject)   // 主题  可以是JSON数据
                .setIssuer(issuer)     // 签发者
                .setIssuedAt(now)      // 签发时间
                .signWith(signatureAlgorithm, secretKey) //使用HS256对称加密算法签名, 第二个参数为秘钥
                .setExpiration(expDate);
    }

    /**
     * 创建token
     *
     * @param id
     * @param subject
     * @param ttlMillis
     * @return
     */
    public static String createJWT(String id, String subject, Long ttlMillis, String secret, String issuer) {
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, id, secret, issuer);// 设置过期时间
        return builder.compact();
    }


    ///**
    // * 生成加密后的秘钥 secretKey
    // *
    // * @return
    // */
    //public static SecretKey generalKey(String secret) {
    //    byte[] encodedKey = Base64.getDecoder().decode(secret);
    //    SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    //    return key;
    //}

    /**
     * 生成 HMAC-SHA256 密钥
     */
    public static SecretKey generalKey(String secret) {
        byte[] encodedKey = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(encodedKey); // HMAC-SHA 密钥
    }


    /**
     * 解析
     *
     * @param jwt
     * @return
     * @throws Exception
     */
    public static Claims parseJWT(String jwt, String secret) throws Exception {
        SecretKey secretKey = generalKey(secret);
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    public static Claims parseJWT(String jwt) throws Exception {
        return parseJWT(jwt, getJWT_KEY());
    }

    public static String getSubjectByParseJWT(String jwt) throws Exception {
        try {
            String subject = parseJWT(jwt).getSubject();
            return subject;
        } catch (Exception e) {
            throw new GlobalCustomException(ApiCode.UNAUTHORIZED);
        }
    }

    /**
     * 判断token是否过期
     *
     * @param token
     * @return
     * @throws Exception
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseJWT(token);
            return isTokenExpired(claims, new Date());
        } catch (Exception e) {
            log.error("token is invalid:{}", e.getMessage());
            return false;
        }

    }

    // 判断JWT是否过期
    public static boolean isTokenExpired(Claims claims) {
        return isTokenExpired(claims, new Date());
    }

    /**
     * 判断JWT是否过期
     *
     * @param claims
     * @param date
     * @return
     */
    public static boolean isTokenExpired(Claims claims, Date date) {
        return claims.getExpiration().before(date);
    }
}
