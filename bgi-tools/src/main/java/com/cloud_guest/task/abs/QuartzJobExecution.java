package com.cloud_guest.task.abs;


import com.cloud_guest.utils.task.JobInvokeUtil;
import org.quartz.JobExecutionContext;

import java.util.Map;

/**
 * 定时任务允许并发
 */
public class QuartzJobExecution extends AbstractQuartzJob {
    @Override
    protected void doExecute(JobExecutionContext context, Map<String, Object> jobMap) throws Exception {
        JobInvokeUtil.invokeMethod(jobMap);
    }
}
