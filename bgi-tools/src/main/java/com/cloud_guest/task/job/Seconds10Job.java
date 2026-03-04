package com.cloud_guest.task.job;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.domain.ApplicationInfo;
import com.cloud_guest.service.ApplicationService;
import com.cloud_guest.service.AuthService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.task.dstributed.DistributedJob;
import com.cloud_guest.utils.ApplicationContextHolder;
import com.cloud_guest.utils.ApplicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * @Author yan
 * @Date 2026/3/2 20:26:08
 * @Description
 */
@Slf4j
@Component
public class Seconds10Job extends DistributedJob {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

    }
}
