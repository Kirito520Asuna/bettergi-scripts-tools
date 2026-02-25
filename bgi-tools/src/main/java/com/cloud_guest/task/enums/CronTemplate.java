package com.cloud_guest.task.enums;

import cn.hutool.core.date.DatePattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @DateTime 2024/6/20 22:52:03
 * @Description
 */
@Getter
@AllArgsConstructor
public enum CronTemplate {
    MINUTE("0 0/%s * * * ?",TimeUnit.MINUTES, DatePattern.UTC_SIMPLE_PATTERN.replace(":ss", "")),
    HOUR("0 0 0/%s * * ?",TimeUnit.HOURS, DatePattern.UTC_SIMPLE_PATTERN.replace(":mm:ss", "")),
    CLOCK("0 0 %s * * ?",null, DatePattern.UTC_SIMPLE_PATTERN.replace(":mm:ss", ""));
    private String cronTemplate;
    private TimeUnit timeUnit;
    private String datePattern;
}
