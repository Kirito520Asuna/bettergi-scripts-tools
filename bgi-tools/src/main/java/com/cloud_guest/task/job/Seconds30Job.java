package com.cloud_guest.task.job;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.abs.service.AbstractKeyService;
import com.cloud_guest.domain.key.KeyInfo;
import com.cloud_guest.task.dstributed.DistributedJob;
import com.cloud_guest.utils.ApplicationContextHolder;
import com.cloud_guest.utils.ModeUtil;
import com.cloud_guest.utils.object.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
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
        AbstractKeyService keyService = SpringUtil.getBean(AbstractKeyService.class);
        ThreadPoolTaskExecutor executor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);
        CompletableFuture.runAsync(() -> {
            log.debug("清理离线");
            ApplicationContextHolder.clearOutlineKeys();
            log.debug("清理重启");
            ApplicationContextHolder.clearRestartKeys();

            if (ModeUtil.isLocal()){
                //本地模式，清理过期key redis模式设置了有效期无需管理
                List<KeyInfo> allExpiredKeyInfoList = keyService.getAllExpiredKeyInfoList();
                if (ObjectUtils.isNotEmpty(allExpiredKeyInfoList)){
                    log.debug("清理过期key");
                    keyService.remove(allExpiredKeyInfoList.stream().map(KeyInfo::getId).toList());
                }
            }
        }, executor);
    }
}
