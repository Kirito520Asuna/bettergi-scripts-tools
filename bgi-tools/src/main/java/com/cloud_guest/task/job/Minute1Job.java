package com.cloud_guest.task.job;


import com.cloud_guest.task.dstributed.DistributedJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Minute1Job extends DistributedJob {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {


    }

}
