package com.cloud_guest.utils.jwt;

/**
 * @Author yan
 * @Date 2026/2/10 12:49:33
 * @Description
 */

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.exception.exceptions.GlobalCustomException;
import com.cloud_guest.properties.auth.AuthProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

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
    //@Resource
    //private AuthProperties authProperties;

    @PostConstruct
    public void init() {
        try {
            AuthProperties.Jwt jwt = fetchAuthProperties().getJwt();
            secret = jwt.getSecret();
            expire = jwt.getExpirationMs();
            expireLong = expire * 30;
        } catch (Exception e) {
            log.error("JwtUtil init error: {}", e.getMessage());
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(fetchAuthProperties().getJwt().getSecret().getBytes());
    }

    public String generateToken(String username) {
        return createJWT(username);
    }

    public String generateToken(String username, Long ttlMillis) {
        return createJWT(username, ttlMillis);
    }

    public String getUsernameFromToken(String token) {
        try {
            return getSubjectByParseJWT(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从已过期的token中提取用户名（忽略过期验证）
     * @param token 过期的JWT token
     * @return 用户名，如果解析失败返回null
     */
    public String getUsernameFromExpiredToken(String token) {
        try {
            // 使用宽松的解析器，允许过期token被解析
            SecretKey secretKey = generalKey(getJWT_KEY());
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            // token过期时，仍然可以从异常中获取claims
            log.debug("token已过期，但仍可提取用户名: {}", e.getClaims().getSubject());
            return e.getClaims().getSubject();
        } catch (Exception e) {
            log.error("无法从token中提取用户名: {}", e.getMessage());
            return null;
        }
    }
    public String getUsernameByToken(String token) {
        try {
            SecretKey secretKey = generalKey(getJWT_KEY());
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("无法从token中提取用户名: {}", e.getMessage());
            return null;
        }
    }
    public boolean validateToken(String token) {
        return isNotTokenExpired(token);
    }

    public static AuthProperties fetchAuthProperties() {
        AuthProperties authProperties = new AuthProperties();
        try {
            authProperties = SpringUtil.getBean(AuthProperties.class);
        } catch (Exception e) {
            log.warn("未找到AuthProperties Bean");
        }
        return authProperties;
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
    public static boolean isNotTokenExpired(String token) {
        try {
            Claims claims = parseJWT(token);
            return isNotTokenExpired(claims, new Date());
        } catch (Exception e) {
            log.error("token is invalid:{}", e.getMessage());
            return false;
        }

    }

    public static boolean isNotTokenExpired(String token, Date date) {
        try {
            Claims claims = parseJWT(token);
            return isNotTokenExpired(claims, date);
        } catch (Exception e) {
            log.error("token is invalid:{}", e.getMessage());
            return false;
        }

    }

    // 判断JWT是否过期
    public static boolean isNotTokenExpired(Claims claims) {
        return isNotTokenExpired(claims, new Date());
    }

    /**
     * 判断JWT未过期
     *
     * @param claims
     * @param date
     * @return
     */
    public static boolean isNotTokenExpired(Claims claims, Date date) {
        Date expiration = claims.getExpiration();
        //expiration<date
        boolean after = expiration.after(date);
        return after;
    }

/*    public static void main(String[] args) {
        String username = "admin";
        Long ttlMillis = 1000 * 60 * 60 * 24 * 30L;
        String jwt = createJWT(username, ttlMillis);
        long currentTimeMillis = System.currentTimeMillis();
        currentTimeMillis -= 1000 * 60 * 60 * 24;
        Date date = new Date(currentTimeMillis);
        boolean notTokenExpired = isNotTokenExpired(jwt, date);
        if (notTokenExpired) {
            System.out.println("即将过期");
        }
    }*/
}
