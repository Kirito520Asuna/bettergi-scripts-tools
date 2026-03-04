package com.cloud_guest.task.job;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.domain.ApplicationInfo;
import com.cloud_guest.service.ApplicationService;
import com.cloud_guest.task.dstributed.DistributedJob;
import com.cloud_guest.utils.ApplicationContextHolder;
import com.cloud_guest.utils.ApplicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * @Author yan
 * @Date 2026/3/2 20:26:08
 * @Description
 */
@Slf4j
@Component
public class Seconds3Job extends DistributedJob {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.debug("加载4s application.yml");
        ApplicationService bean = SpringUtil.getBean(ApplicationService.class);
        bean.loadApplicationYml(4000l);
        ThreadPoolTaskExecutor executor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);
        CompletableFuture.runAsync(() -> {
            //上报在线
            ApplicationInfo applicationInfo = ApplicationUtil.getApplicationInfo();
            if (applicationInfo != null) {
                //避免污染缓存数据
                ApplicationInfo reportedOnline = applicationInfo.toReportedOnline();
                log.debug("上报在线:{}", reportedOnline);
                ApplicationContextHolder.reportedOnline(reportedOnline);

                // 按顺序执行，确保数据一致性
                log.debug("检查在线");
                ApplicationContextHolder.checkAndGetOnline(null);
            }
        }, executor);

    }
}
