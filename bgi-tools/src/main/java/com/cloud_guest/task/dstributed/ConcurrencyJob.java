package com.cloud_guest.task.dstributed;

import cn.hutool.core.date.DatePattern;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author yan
 * @Date 2026/5/29 20:14:07
 * @Description
 */
// 允许并发执行
// 持久化
@PersistJobDataAfterExecution
@Slf4j
public class ConcurrencyJob extends QuartzJobBean {
    public static final String TASK_LOG_KEY = "[定时任务]";

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String taskName = context.getJobDetail().getJobDataMap().getString("name");
        log.debug("{} ===> Quartz job, time:{} ,name:{} <===", TASK_LOG_KEY, DateTimeFormatter.ofPattern(DatePattern
                .NORM_DATETIME_PATTERN).format(LocalDateTime.now()), taskName);
    }
}
