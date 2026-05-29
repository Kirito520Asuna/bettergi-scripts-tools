package com.cloud_guest.task.config;


import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.entitys.pojo.SysJob;
import com.cloud_guest.service.SysJobService;
import com.cloud_guest.task.domain.TaskDef;
import com.cloud_guest.task.domain.TaskInfo;
import com.cloud_guest.task.enums.CronTemplate;
import com.cloud_guest.task.enums.QuartzGroup;
import com.cloud_guest.task.enums.QuartzName;
import com.cloud_guest.task.job.*;
import com.cloud_guest.task.util.QuartzUtil;
import com.cloud_guest.utils.bean.CustomBeanUtils;
import com.cloud_guest.utils.task.ScheduleUtils;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yao
 * @date 2024/5/28 17:52
 */
@Configuration
@Slf4j
public class QuartzConfig {
    @Resource
    private SysJobService sysJobService;
    public static final String TASK_LOG_KEY = "[定时任务]";

    // 如果你已经有 QuartzName 枚举，可以继续用它
    // 这里我们用一个简单的定义类，更容易阅读和扩展
    public static List<TaskInfo> TASKS = new ArrayList<>();

    public void initTasks() {
        List<TaskInfo> tasks = new ArrayList<>();
        tasks.add(new TaskDef(QuartzName.SECONDS_1, QuartzGroup.DEFAULT, Seconds1Job.class, CronTemplate.SECONDS).buildToTaskInfo());
        tasks.add(new TaskDef(QuartzName.SECONDS_3, QuartzGroup.DEFAULT, Seconds3Job.class, CronTemplate.SECONDS).buildToTaskInfo());
        tasks.add(new TaskDef(QuartzName.SECONDS_10, QuartzGroup.DEFAULT, Seconds10Job.class, CronTemplate.SECONDS).buildToTaskInfo());
        tasks.add(new TaskDef(QuartzName.SECONDS_30, QuartzGroup.DEFAULT, Seconds30Job.class, CronTemplate.SECONDS).buildToTaskInfo());
        tasks.add(new TaskDef(QuartzName.MINUTE_1, QuartzGroup.DEFAULT, Minute1Job.class, CronTemplate.MINUTE).buildToTaskInfo());
        tasks.add(new TaskDef(QuartzName.CLOCK_0, QuartzGroup.DEFAULT, Clock0Job.class, CronTemplate.CLOCK).buildToTaskInfo());
        TASKS.addAll(tasks);
    }

    @PostConstruct
    public void init() throws SchedulerException {
        initTasks();
        Scheduler scheduler = SpringUtil.getBean(Scheduler.class);
        scheduler.clear();
        log.info("{}-{}开始动态注册 Quartz 定时任务... 共 {} 个", TASK_LOG_KEY,"[系统级]", TASKS.size());

        for (TaskInfo taskInfo : TASKS) {
            try {
                QuartzUtil.registerTask(taskInfo);
                log.info("{}任务注册成功：{} → {}", TASK_LOG_KEY, taskInfo.getName(), taskInfo.getCronExpression());
            } catch (Exception e) {
                log.error("{}任务注册失败：{}，原因：{}", TASK_LOG_KEY, taskInfo.getName(), e.getMessage(), e);
            }
        }

        try {
            List<SysJob> jobList =  sysJobService.list();
            //SysJob sysJob = new SysJob();//测试通过
            //sysJob.setJobName("定时备份");
            //sysJob.setJobId(1L);
            //sysJob.setJobGroup("定时备份");
            //sysJob.setStatus("0");
            //sysJob.setInvokeTarget("dataBackupRecoveryServiceImpl.backupTest()");
            //sysJob.setCronExpression("0/3 * * * * ?");
            //sysJob.setMisfirePolicy("1");
            //sysJob.setConcurrent("0");
            //sysJob.setRemark("定时备份");
            //jobList.add(sysJob);

            log.info("{}-{}开始动态注册 Quartz 定时任务... 共 {} 个", TASK_LOG_KEY,"[数据级]", jobList.size());
            List<Map<String, Object>> jobMapList = jobList.stream()
                    .map(job -> {
                        Map<String, Object> jobMap = Maps.newLinkedHashMap();
                        CustomBeanUtils.copyPropertiesIgnoreNull(job, jobMap);
                        return jobMap;
                    }).collect(Collectors.toList());

            for (Map<String, Object> jobMap : jobMapList) {
                ScheduleUtils.createScheduleJob(scheduler, jobMap);
            }
        } catch (Exception e) {
            log.error("quartz任务初始化失败error: {}", e.getMessage());
        }


        // 可选：启动时检查 scheduler 是否已经启动
        if (!scheduler.isStarted()) {
            scheduler.start();
        }

        log.info("所有 Quartz 任务动态注册完成");
    }

}
