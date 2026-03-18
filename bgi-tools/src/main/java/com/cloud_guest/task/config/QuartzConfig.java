package com.cloud_guest.task.config;


import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.task.domain.TaskDef;
import com.cloud_guest.task.domain.TaskInfo;
import com.cloud_guest.task.enums.CronTemplate;
import com.cloud_guest.task.enums.QuartzGroup;
import com.cloud_guest.task.enums.QuartzName;
import com.cloud_guest.task.job.*;
import com.cloud_guest.task.util.QuartzUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * @author Yao
 * @date 2024/5/28 17:52
 */
@Configuration
@Slf4j
public class QuartzConfig {
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
        log.info("{}开始动态注册 Quartz 定时任务... 共 {} 个", TASK_LOG_KEY, TASKS.size());

        for (TaskInfo taskInfo : TASKS) {
            try {
                QuartzUtil.registerTask(taskInfo);
                log.info("{}任务注册成功：{} → {}", TASK_LOG_KEY, taskInfo.getName(), taskInfo.getCronExpression());
            } catch (Exception e) {
                log.error("{}任务注册失败：{}，原因：{}", TASK_LOG_KEY, taskInfo.getName(), e.getMessage(), e);
            }
        }

        // 可选：启动时检查 scheduler 是否已经启动
        if (!scheduler.isStarted()) {
            scheduler.start();
        }

        log.info("所有 Quartz 任务动态注册完成");
    }

}
