package com.cloud_guest.redis.abs.ban;


import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.redis.ban.BanType;
import com.cloud_guest.redis.service.RedisService;

import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2025/5/20 15:55:27
 * @Description
 */
public interface BanManager {
    String BANNED_IPS_KEY = "banned:ips"; // 全局 IP 封禁
    String BANNED_INTERFACES_KEY = "banned:interfaces"; // 接口封禁
    String BANNED_IP_INTERFACE_PREFIX = "banned:ip:interface:"; // IP+接口封禁前缀
    String RATE_LIMIT_PREFIX = "rate:limit:"; // 请求计数前缀
    String BANNED_IP_INTERFACE_TEMPLATE = BANNED_IP_INTERFACE_PREFIX + "%s&%s";

    default RedisService getRedisService() {
        return SpringUtil.getBean(RedisService.class);
    }
    // 封禁 IP（全局）
    void banIP(String ipAddress, long duration, TimeUnit timeUnit);

    // 检查 IP 是否被全局封禁
    boolean isIPBanned(String ipAddress);

    // 解除 IP 封禁
    void unbanIP(String ipAddress);

    // 封禁接口
    void banInterface(String interfaceName, long duration, TimeUnit timeUnit);

    // 检查接口是否被封禁
    boolean isInterfaceBanned(String interfaceName);

    // 解除接口封禁
    void unbanInterface(String interfaceName);

    // 封禁 IP+接口
    void banIPInterface(String ipAddress, String interfaceName, long duration, TimeUnit timeUnit);

    // 检查 IP+接口是否被封禁
    boolean isIPInterfaceBanned(String ipAddress, String interfaceName);

    // 解除 IP+接口封禁
    void unbanIPInterface(String ipAddress, String interfaceName);

    // 检查请求频率并自动封禁
    boolean checkAndBan(String key, long windowSeconds, long maxRequests, long banDuration, TimeUnit timeUnit, BanType banType, String ipAddress, String interfaceName);
}
