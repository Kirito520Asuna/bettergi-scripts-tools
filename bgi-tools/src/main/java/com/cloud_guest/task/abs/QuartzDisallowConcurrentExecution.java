package com.cloud_guest.task.abs;


import com.cloud_guest.utils.task.JobInvokeUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

import java.util.Map;

@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob {
    @Override
    protected void doExecute(JobExecutionContext context, Map<String, Object> jobMap) throws Exception {
        JobInvokeUtil.invokeMethod(jobMap);
    }
}
