package com.cloud_guest.task.job;

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
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @Author yan
 * @Date 2026/3/2 20:26:08
 * @Description
 */
@Slf4j
@Component
public class Seconds1Job extends DistributedJob {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //设置超时
        Long timeout = null;
        boolean taskSettings = ApplicationContextHolder.getTaskSettings();
        if (!taskSettings) {
            Optional<TaskInfo> first = QuartzConfig.TASKS.stream().filter(task -> ObjectUtils.equals(task.getJobClass(), Seconds10Job.class)).findFirst();
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
                ApplicationUtil.setCronExpression(cronExpression);
                ApplicationUtil.setTimeout(timeout);
            }
        }

    }
}
