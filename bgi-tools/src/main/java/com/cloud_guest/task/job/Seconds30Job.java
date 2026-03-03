package com.cloud_guest.task.job;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.domain.ApplicationInfo;
import com.cloud_guest.task.config.QuartzConfig;
import com.cloud_guest.task.domain.TaskInfo;
import com.cloud_guest.task.dstributed.DistributedJob;
import com.cloud_guest.task.util.QuartzUtil;
import com.cloud_guest.utils.ApplicationContextHolder;
import com.cloud_guest.utils.ApplicationUtil;
import com.cloud_guest.utils.object.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @Author yan
 * @Date 2026/3/2 20:26:08
 * @Description
 */
@Slf4j
@Component
public class Seconds30Job extends DistributedJob {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //设置超时
        Long timeout = null;
        boolean taskSettings = ApplicationContextHolder.getTaskSettings();
        if (!taskSettings) {
            Optional<TaskInfo> first = QuartzConfig.TASKS.stream().filter(task -> ObjectUtils.equals(task.getJobClass(), Minute1Job.class)).findFirst();
            TaskInfo taskInfo = null;
            if (first.isPresent()) {
                taskInfo = first.get();
            }
            if (taskInfo == null) {
                return;
            }
            String cronExpression = taskInfo.getCronExpression();
            timeout = QuartzUtil.getNextIntervalMillis(cronExpression);
            if (ObjectUtils.isNotEmpty(timeout)) {
                timeout += 20;
                ApplicationContextHolder.setReportedOnlineTimeout(timeout);
                ApplicationContextHolder.setTaskSettings(!taskSettings);
            }
        }
        ThreadPoolTaskExecutor executor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);
        CompletableFuture.runAsync(() -> {
            // 按顺序执行，确保数据一致性
            log.debug("检查在线");
            ApplicationContextHolder.checkAndGetOnline(null);
            log.debug("清理离线");
            ApplicationContextHolder.clearOutlineKeys();
            log.debug("清理重启");
            ApplicationContextHolder.clearRestartKeys();
        }, executor);
    }
}
