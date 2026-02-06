package com.cloud_guest.redis.ban;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author yan
 * @Date 2025/5/20 14:25:37
 * @Description
 */
@Getter
@AllArgsConstructor
public enum BanType {
    IP,         // 全局 IP 封禁
    INTERFACE,  // 接口封禁
    IP_INTERFACE // IP+接口封禁
}