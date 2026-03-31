package com.cloud_guest.properties.auth;

import cn.hutool.extra.spring.SpringUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/3/31 18:30:52
 * @Description
 */
@Component
@ConfigurationProperties(prefix = "sign.api")
@Data
public class ApiProperties {
    /**
     * 微服务名称
     */
    String name;
    /**
     * 签名盐值
     */
    String salt;
    /**
     * 签名参数名称
     */
    String signAsName = "sign";
    /**
     * 时间戳参数名称
     */
    String timestampAsName = "timestamp";
    /**
     * 匹配的路径
     */
    List<String> pathsToMatch = Arrays.asList("/api/**").stream().collect(Collectors.toList());

    /**
     * IP白名单
     */
    List<String> ipWhitelist = new ArrayList<>();
    /**
     * IP黑名单
     */
    List<String> ipBlackList = new ArrayList<>();
    /**
     * 是否开启签名校验
     */
    Boolean signEnable = false;
    /**
     * 是否开启多签名校验
     */
    Boolean signMultipleEnable = false;
    /**
     * 签名超时时间 单位分钟
     */
    Long signTimeOut = 10l;
    /**
     * 是否开启对称加密
     */
    Boolean enableDoubleSymmetricEncryption = false;
    /**
     * 对称加密参数名称
     */
    String encryptionAsName = "X-Bgi-Tools-Encryption-Client-Public-Key";
    /**
     * 对称加密id参数名称
     */
    String idAsName = "X-Bgi-Tools-Encryption-Id";
    /**
     * 指定无需对称加密的路径
     */
    List<String> encryptionPathsToExclude = new ArrayList<>();
}
