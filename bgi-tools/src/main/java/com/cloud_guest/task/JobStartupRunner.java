package com.cloud_guest.task;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.task.domain.QuartzConfig;
import com.cloud_guest.task.domain.QuartzObject;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/25 11:18:27
 * @Description
 */
@Component
@Slf4j
public class JobStartupRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Scheduler scheduler = SpringUtil.getBean(Scheduler.class);
        List<QuartzObject> quartzListAll = new ArrayList<>();
        quartzListAll.addAll(QuartzConfig.quartzObjectList);

        quartzListAll.stream().forEach(quartzObject -> {
            //CronScheduleBuilder scheduleBuilder = quartzObject.getScheduleBuilder();
            TriggerKey triggerKey = quartzObject.getTriggerKey();
            JobKey jobKey = quartzObject.getJobKey();
            Trigger trigger = quartzObject.getTrigger();
            JobDetail jobDetail = quartzObject.getJobDetail();
            try {
                CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
                if (null == cronTrigger && jobDetail != null && trigger != null) {
                    scheduler.scheduleJob(jobDetail, trigger);
                    log.debug("==> Quartz 创建了job: {} <==", jobDetail.getKey());
                } else {
                    if (jobDetail != null) {
                        log.debug("==> {} job已存在 <==", jobDetail.getKey());
                    } else {
                        log.debug("==> {} job未初始化 <==",jobKey);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        scheduler.start();
    }
}
