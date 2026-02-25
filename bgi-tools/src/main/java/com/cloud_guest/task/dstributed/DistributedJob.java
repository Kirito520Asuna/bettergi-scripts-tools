package com.cloud_guest.task.dstributed;

import cn.hutool.core.date.DatePattern;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author yan
 * @DateTime 2024/6/23 14:56:39
 * @Description 分布式 Job
 */
// 持久化
@PersistJobDataAfterExecution
// 禁止并发执行
@DisallowConcurrentExecution
@Slf4j
public class DistributedJob extends QuartzJobBean {
    public static final String TASK_LOG_KEY = "[定时任务]";

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String taskName = context.getJobDetail().getJobDataMap().getString("name");
        log.debug("{} ===> Quartz job, time:{} ,name:{} <===", TASK_LOG_KEY, DateTimeFormatter.ofPattern(DatePattern
                .NORM_DATETIME_PATTERN).format(LocalDateTime.now()), taskName);
    }
}
