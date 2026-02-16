package com.cloud_guest.service;


import com.cloud_guest.domain.dto.CronDto;
import com.cloud_guest.vo.CronVo;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/1/12 18:15:13
 * @Description
 */
public interface CronService {
    /**
     * 根据给定的cron表达式，在指定的时间范围内查找下一次执行时间
     *
     * @param cronExpression cron表达式，用于定义执行时间的规则
     * @param startTimeMs    开始时间，单位为毫秒，表示查找范围的起始时间点
     * @param endTimeMs      结束时间，单位为毫秒，表示查找范围的结束时间点
     * @return 返回在指定范围内找到的最近一次执行时间，如果未找到则返回null
     */
    Long findNearestExecutionAfter(
            String cronExpression,  // cron表达式字符串，定义了定时任务的执行规则
            long startTimeMs,      // 开始时间，毫秒级时间戳
            long endTimeMs);       // 结束时间，毫秒级时间戳

    List<CronVo> findNearestExecutionAfterAll(List<CronDto> cronList);
}
