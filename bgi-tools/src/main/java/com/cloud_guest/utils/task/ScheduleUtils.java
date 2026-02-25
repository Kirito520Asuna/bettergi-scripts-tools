package com.cloud_guest.utils.task;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.constants.schedule.ScheduleConstants;
import com.cloud_guest.exception.exceptions.TaskException;
import com.cloud_guest.task.abs.QuartzDisallowConcurrentExecution;
import com.cloud_guest.task.abs.QuartzJobExecution;
import org.quartz.*;

import java.util.Map;

/**
 * @Author yan
 * @Date 2025/4/9 18:29:14
 * @Description
 */
public class ScheduleUtils {
    /**
     * 得到quartz任务类
     *
     * @param jobMap 执行计划
     * @return 具体执行任务类
     */
    private static Class<? extends Job> getQuartzJobClass(Map<String, Object> jobMap) {
        boolean isConcurrent = "0".equals(jobMap.get("concurrent"));
        return isConcurrent ? QuartzJobExecution.class : QuartzDisallowConcurrentExecution.class;
    }

    /**
     * 构建任务触发对象
     */
    public static TriggerKey buildDefaultTriggerKey(Long id, String jobGroup) {
        return buildTriggerKey(ScheduleConstants.TASK_CLASS_NAME + id, jobGroup);
    }

    /**
     * 构建任务触发对象
     */
    public static TriggerKey buildTriggerKey(String name, String jobGroup) {
        return TriggerKey.triggerKey(name, jobGroup);
    }

    /**
     * 构建任务键对象
     */
    public static JobKey buildDefaultJobKey(Long id, String jobGroup) {
        return buildJobKey(ScheduleConstants.TASK_CLASS_NAME + id, jobGroup);
    }

    /**
     * 构建任务键对象
     */
    public static JobKey buildJobKey(String name, String jobGroup) {
        return JobKey.jobKey(name, jobGroup);
    }


    /**
     * 创建定时任务
     */
    public static void createScheduleJob(Scheduler scheduler, Map<String, Object> jobMap) throws SchedulerException, TaskException {
        Class<? extends Job> jobClass = getQuartzJobClass(jobMap);
        // 构建job信息
        Long jobId = Long.parseLong(String.valueOf(jobMap.get("jobId")));
        String jobGroup = (String) jobMap.get("jobGroup");
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(buildDefaultJobKey(jobId, jobGroup)).build();

        // 表达式调度构建器
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule((String) jobMap.get("cronExpression"));
        cronScheduleBuilder = handleCronScheduleMisfirePolicy(jobMap, cronScheduleBuilder);

        // 按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(buildDefaultTriggerKey(jobId, jobGroup))
                .withSchedule(cronScheduleBuilder).build();

        // 放入参数，运行时的方法可以获取
        jobDetail.getJobDataMap().put(ScheduleConstants.TASK_PROPERTIES, jobMap);

        // 判断是否存在
        if (scheduler.checkExists(buildDefaultJobKey(jobId, jobGroup))) {
            // 防止创建时存在数据问题 先移除，然后在执行创建操作
            scheduler.deleteJob(buildDefaultJobKey(jobId, jobGroup));
        }

        // 判断任务是否过期
        if (null != CronUtils.getNextExecution((String) jobMap.get("cronExpression"))) {
            // 执行调度任务
            scheduler.scheduleJob(jobDetail, trigger);
        }

        // 暂停任务
        if (StrUtil.equals(ScheduleConstants.Status.PAUSE.getValue(), (String) jobMap.get("status"))) {
            scheduler.pauseJob(ScheduleUtils.buildDefaultJobKey(jobId, jobGroup));
        }
    }


    /**
     * 设置定时任务策略
     */
    public static CronScheduleBuilder handleCronScheduleMisfirePolicy(Map<String, Object> jobMap, CronScheduleBuilder cb)
            throws TaskException {
        String misfirePolicy = (String) jobMap.get("misfirePolicy");
        switch (misfirePolicy) {
            case ScheduleConstants.MISFIRE_DEFAULT:
                return cb;
            case ScheduleConstants.MISFIRE_IGNORE_MISFIRES:
                return cb.withMisfireHandlingInstructionIgnoreMisfires();
            case ScheduleConstants.MISFIRE_FIRE_AND_PROCEED:
                return cb.withMisfireHandlingInstructionFireAndProceed();
            case ScheduleConstants.MISFIRE_DO_NOTHING:
                return cb.withMisfireHandlingInstructionDoNothing();
            default:
                String message = "The task misfire policy '" + misfirePolicy
                        + "' cannot be used in cron schedule tasks";
                throw new TaskException(message);
        }
    }

}
