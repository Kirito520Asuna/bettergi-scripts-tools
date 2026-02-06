package com.cloud_guest.redis.ban;

import com.cloud_guest.redis.abs.ban.BanManager;
import com.cloud_guest.redis.service.RedisService;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RSet;

import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2025/5/20 14:27:11
 * @Description
 */
public class SimpleBanManager implements BanManager {

    public SimpleBanManager() {
    }

    // 封禁 IP（全局）
    @Override
    public void banIP(String ipAddress, long duration, TimeUnit timeUnit) {
        RedisService redisService = getRedisService();
        RSet<String> bannedIps = redisService.getSetByRS(BANNED_IPS_KEY);
        bannedIps.add(ipAddress);
        if (duration > 0) {
            bannedIps.expire(duration, timeUnit);
        }
    }

    // 检查 IP 是否被全局封禁
    @Override
    public boolean isIPBanned(String ipAddress) {
        RedisService redisService = getRedisService();
        RSet<String> bannedIps = redisService.getSetByRS(BANNED_IPS_KEY);
        return bannedIps.contains(ipAddress);
    }

    // 解除 IP 封禁
    @Override
    public void unbanIP(String ipAddress) {
        RedisService redisService = getRedisService();
        RSet<String> bannedIps = redisService.getSetByRS(BANNED_IPS_KEY);
        bannedIps.remove(ipAddress);
    }

    // 封禁接口
    @Override
    public void banInterface(String interfaceName, long duration, TimeUnit timeUnit) {
        RedisService redisService = getRedisService();
        RSet<String> bannedInterfaces = redisService.getSetByRS(BANNED_INTERFACES_KEY);
        bannedInterfaces.add(interfaceName);
        if (duration > 0) {
            bannedInterfaces.expire(duration, timeUnit);
        }
    }

    // 检查接口是否被封禁
    @Override
    public boolean isInterfaceBanned(String interfaceName) {
        RedisService redisService = getRedisService();
        RSet<String> bannedInterfaces = redisService.getSetByRS(BANNED_INTERFACES_KEY);
        return bannedInterfaces.contains(interfaceName);
    }

    // 解除接口封禁
    @Override
    public void unbanInterface(String interfaceName) {
        RedisService redisService = getRedisService();
        RSet<String> bannedInterfaces = redisService.getSetByRS(BANNED_INTERFACES_KEY);
        bannedInterfaces.remove(interfaceName);
    }

    // 封禁 IP+接口
    @Override
    public void banIPInterface(String ipAddress, String interfaceName, long duration, TimeUnit timeUnit) {
        String key = String.format(BANNED_IP_INTERFACE_TEMPLATE, ipAddress, interfaceName);
        RedisService redisService = getRedisService();
        redisService.getBucketByRS(key).set(true);
        if (duration > 0) {
            redisService.getBucketByRS(key).expire(duration, timeUnit);
        }
    }

    // 检查 IP+接口是否被封禁
    @Override
    public boolean isIPInterfaceBanned(String ipAddress, String interfaceName) {
        String key = String.format(BANNED_IP_INTERFACE_TEMPLATE, ipAddress, interfaceName);
        RedisService redisService = getRedisService();
        return redisService.getBucketByRS(key).isExists();
    }

    // 解除 IP+接口封禁
    @Override
    public void unbanIPInterface(String ipAddress, String interfaceName) {
        String key = String.format(BANNED_IP_INTERFACE_TEMPLATE, ipAddress, interfaceName);
        RedisService redisService = getRedisService();
        redisService.getBucketByRS(key).delete();
    }

    // 检查请求频率并自动封禁
    @Override
    public boolean checkAndBan(String key, long windowSeconds, long maxRequests, long banDuration, TimeUnit timeUnit, BanType banType, String ipAddress, String interfaceName) {

/*
        if (isIPBanned(ipAddress)) {
            return true; // IP 已被封禁
        } else if (ObjectUtil.isNotEmpty(interfaceName) && isInterfaceBanned(interfaceName)) {
            return true; // 接口已被封禁
        } else if (ObjectUtil.isNotEmpty(interfaceName) && isIPInterfaceBanned(ipAddress, interfaceName)) {
            return true; // IP+接口已被封禁
        }
*/

        String rateKey = RATE_LIMIT_PREFIX + key;
        RedisService redisService = getRedisService();
        RAtomicLong counter = redisService.getAtomicLongByRS(rateKey);
        long count = counter.incrementAndGet();

        if (count == 1) {
            counter.expire(windowSeconds, TimeUnit.SECONDS);
        }

        if (count > maxRequests) {
            // 触发自动封禁
            switch (banType) {
                case IP:
                    banIP(ipAddress, banDuration, timeUnit);
                    break;
                case INTERFACE:
                    banInterface(interfaceName, banDuration, timeUnit);
                    break;
                case IP_INTERFACE:
                    banIPInterface(ipAddress, interfaceName, banDuration, timeUnit);
                    break;
            }
            return true; // 已封禁
        }
        return false; // 未封禁
    }
}