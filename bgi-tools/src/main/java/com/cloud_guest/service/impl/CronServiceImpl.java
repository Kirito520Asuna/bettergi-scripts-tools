package com.cloud_guest.service.impl;

import com.cloud_guest.service.CronService;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * @Author yan
 * @Date 2026/1/12 18:17:41
 * @Description
 */
@Service
public class CronServiceImpl implements CronService {
    /**
     * 查找 [startTime, endTime] 区间内，离 startTime 最近的一次 cron 执行时间
     *
     * @param cronExpression cron 表达式（Quartz 风格）
     * @param startTimeMs    开始时间戳（毫秒，Unix timestamp）
     * @param endTimeMs      结束时间戳（毫秒，Unix timestamp）
     * @return 最近的执行时间戳（毫秒），如果区间内没有则返回 empty
     */
    @Override
    public Long findNearestExecutionAfter(
            String cronExpression,
            long startTimeMs,
            long endTimeMs) {

        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

        Cron cron = parser.parse(cronExpression).validate();
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        // long → Instant → ZonedDateTime
        ZonedDateTime from = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTimeMs), ZoneId.of("Asia/Shanghai"));
        ZonedDateTime to = ZonedDateTime.ofInstant(Instant.ofEpochMilli(endTimeMs), ZoneId.of("Asia/Shanghai"));

        // 寻找下一个执行时间
        Optional<ZonedDateTime> next = executionTime.nextExecution(from);

        // 如果存在且在 endTime 之前/等于，返回对应的毫秒时间戳
        return next.filter(candidate -> !candidate.isAfter(to))
                .map(ZonedDateTime::toInstant)
                .map(Instant::toEpochMilli).orElseGet(null);


    }
}
