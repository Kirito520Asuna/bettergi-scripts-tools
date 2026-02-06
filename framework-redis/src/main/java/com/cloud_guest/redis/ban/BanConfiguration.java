package com.cloud_guest.redis.ban;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author yan
 * @Date 2025/5/20 14:57:10
 * @Description
 */
@Data
@AllArgsConstructor
public class BanConfiguration {
    @Value("${ip.ban.global-enabled:false}")
    private boolean globalBanEnabled;
    @Value("${ip.whitelist:}")
    private String whitelistIps;
    @Value("${ip.blacklist:}")
    private String blacklistIps;

    private Set<String> whitelist;
    private Set<String> blacklist;

    public BanConfiguration() {
        if (this.whitelistIps == null) {
            this.whitelistIps = "";
        }
        if (this.blacklistIps == null) {
            this.blacklistIps = "";
        }
        // 初始化白名单和黑名单
        this.whitelist = new HashSet<>(Arrays.asList(whitelistIps.split(",")));
        this.blacklist = new HashSet<>(Arrays.asList(blacklistIps.split(",")));
        // 移除空字符串
        this.whitelist.remove("");
        this.blacklist.remove("");
    }
}

