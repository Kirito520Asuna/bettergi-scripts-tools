package com.cloud_guest.task.domain;


import com.cloud_guest.task.enums.CronTemplate;
import com.cloud_guest.task.enums.QuartzGroup;
import com.cloud_guest.task.enums.QuartzName;
import com.cloud_guest.task.job.Clock0Job;
import com.cloud_guest.task.job.Minute1Job;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yao
 * @date 2024/5/28 17:52
 */
@Configuration
@Slf4j
public class QuartzConfig {
    public static Map<QuartzName, QuartzObject> QUARTZ_OBJECT_MAP = new HashMap<>();
    public static Map<Class<Job>, QuartzName> JOB_CLASS_QUARTZ_NAME_MAP = new HashMap<>();
    public static List<QuartzObject> quartzObjectList = new ArrayList<>();
    private static final Map<QuartzName, JobKey> JOB_KEY_MAP = new HashMap<>();
    private static final Map<QuartzName, CronScheduleBuilder> CRON_SCHEDULE_MAP = new HashMap<>();
    private static final Map<QuartzName, TriggerKey> TRIGGER_KEY_MAP = new EnumMap<>(QuartzName.class);
    private Map<QuartzName, JobDetail> JOB_DETAIL_MAP = new HashMap<>();

    @PostConstruct
    public void init() {
        Map<QuartzName, Class> quartzClassMap = new LinkedHashMap<>();
        quartzClassMap.put(QuartzName.MINUTE_1, Minute1Job.class);
        quartzClassMap.put(QuartzName.CLOCK_0, Clock0Job.class);

        log.debug("~~~~~~~~~~~~~~~~~~QuartzConfig init~~~~~~~~~~~~~~~~~~");
        QuartzGroup quartzGroup = QuartzGroup.DEFAULT;
        String group = quartzGroup.name();
        Arrays.stream(QuartzName.values()).collect(Collectors.toList()).stream().forEach(quartzName -> {
            String name = quartzName.name();
            QuartzObject quartzObject = getQuartzObject(quartzName);

            JobKey jobKey = JobKey.jobKey(name, group);
            TriggerKey triggerKey = TriggerKey.triggerKey(name, group);

            //JOB_KEY_MAP.put(quartzName, jobKey);
            //TRIGGER_KEY_MAP.put(quartzName, triggerKey);
            String[] split = name.split("_");
            CronTemplate cronTemplate = CronTemplate.valueOf(split[0]);
            String value = split[1];
            String cronExpression = String.format(cronTemplate.getCronTemplate(), value);
            CronScheduleBuilder scheduleBuilder = buildCronScheduleBuilder(cronExpression);
            //CRON_SCHEDULE_MAP.put(quartzName, scheduleBuilder);


            quartzObject
                    .setQuartzGroup(quartzGroup)
                    .setQuartzName(quartzName)
                    .setCronTemplate(cronTemplate)
                    .setValue(value)
                    .setJobKey(jobKey)
                    .setTriggerKey(triggerKey)
                    .setScheduleBuilder(scheduleBuilder);
            QUARTZ_OBJECT_MAP.put(quartzName, quartzObject);
        });

        quartzClassMap.keySet().stream().forEach(quartzName -> {
            setClockJobClass(quartzName, quartzClassMap.get(quartzName));
        });

        List<QuartzObject> quartzObjects = QUARTZ_OBJECT_MAP.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
        quartzObjectList.addAll(quartzObjects);

        quartzObjectList.stream().forEach(quartzObject -> {
            JOB_CLASS_QUARTZ_NAME_MAP.put(quartzObject.getClockJobClass(), quartzObject.getQuartzName());
        });
        log.debug("~~~~~~~~~~~~~~~~~~QuartzConfig init end~~~~~~~~~~~~~~~~~~");
    }

    public static QuartzObject getQuartzObject(QuartzName quartzName) {
        QuartzObject quartzObject = QUARTZ_OBJECT_MAP.get(quartzName);
        if (quartzObject == null) {
            quartzObject = new QuartzObject();
            QUARTZ_OBJECT_MAP.put(quartzName, quartzObject);
        }
        return QUARTZ_OBJECT_MAP.get(quartzName);
    }

    public static QuartzObject setClockJobClass(QuartzName quartzName, Class clockJobClass) {
        QuartzObject quartzObject = getQuartzObject(quartzName);
        QUARTZ_OBJECT_MAP.put(quartzName, quartzObject.setClockJobClass(clockJobClass));
        return QUARTZ_OBJECT_MAP.get(quartzName);
    }

    public static CronScheduleBuilder buildCronScheduleBuilder(String cronExpression) {
        return CronScheduleBuilder.cronSchedule(cronExpression);
    }

    public JobDetail buildNewJobDetail(Class clockJob, JobKey jobKey) {
        return JobBuilder.newJob(clockJob).withIdentity(jobKey).storeDurably().build();
    }

    public Trigger buildNewTrigger(CronScheduleBuilder scheduleBuilder, JobKey jobKey, TriggerKey triggerKey) {
        return TriggerBuilder.newTrigger().forJob(jobKey).withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
    }

    public JobDetail buildNewJobDetail(Class clockJob, QuartzName quartzName) {
        QuartzObject quartzObject = getQuartzObject(quartzName);
        JobKey jobKey = quartzObject.getJobKey();
        JobDetail jobDetail = buildNewJobDetail(clockJob, jobKey);
        //JOB_DETAIL_MAP.put(quartzName, jobDetail);
        QUARTZ_OBJECT_MAP.put(quartzName, quartzObject.setJobDetail(jobDetail));
        return jobDetail;
    }

    public JobDetail buildNewJobDetail(QuartzName quartzName) {
        QuartzObject quartzObject = getQuartzObject(quartzName);
        Class clockJobClass = quartzObject.getClockJobClass();
        JobKey jobKey = quartzObject.getJobKey();
        JobDetail jobDetail = buildNewJobDetail(clockJobClass, jobKey);
        //JOB_DETAIL_MAP.put(quartzName, jobDetail);
        QUARTZ_OBJECT_MAP.put(quartzName, quartzObject.setJobDetail(jobDetail));
        return jobDetail;
    }

    public Trigger buildNewTrigger(QuartzName quartzName) {
        QuartzObject quartzObject = getQuartzObject(quartzName);
        CronScheduleBuilder build = quartzObject.getScheduleBuilder();
        JobKey jobKey = quartzObject.getJobKey();
        TriggerKey triggerKey = quartzObject.getTriggerKey();
        //CronScheduleBuilder build = CRON_SCHEDULE_MAP.get(quartzName);
        //JobKey jobKey = JOB_KEY_MAP.get(quartzName);
        //TriggerKey triggerKey = TRIGGER_KEY_MAP.get(quartzName);
        Trigger trigger = buildNewTrigger(build, jobKey, triggerKey);
        QUARTZ_OBJECT_MAP.put(quartzName, quartzObject.setTrigger(trigger));
        return trigger;
    }

    public Trigger buildNewTrigger(JobDetail jobDetail, QuartzName quartzName) {
        QuartzObject quartzObject = getQuartzObject(quartzName);

        CronScheduleBuilder build = quartzObject.getScheduleBuilder();
        //JobKey jobKey = quartzObject.getJobKey();
        TriggerKey triggerKey = quartzObject.getTriggerKey();

        //CronScheduleBuilder build = CRON_SCHEDULE_MAP.get(quartzName);
        //JobKey jobKey = JOB_KEY_MAP.get(quartzName);
        //TriggerKey triggerKey = TRIGGER_KEY_MAP.get(quartzName);
        return TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(triggerKey).withSchedule(build).build();
    }

    @Bean
    public JobDetail clock0JobDetail() {
        return buildNewJobDetail(QuartzName.CLOCK_0);
    }

    @Bean
    public Trigger clock0Trigger() {
        return buildNewTrigger(QuartzName.CLOCK_0);
    }

    @Bean
    public JobDetail minute1JobDetail() {
        //log.debug("~~~~~~~~~~~~~~~~~~minute1JobDetail~~~~~~~~~~~~~~~~~~");
        return buildNewJobDetail(QuartzName.MINUTE_1);
    }

    @Bean
    public Trigger minute1Trigger() {
        //log.debug("~~~~~~~~~~~~~~~~~~minute1Trigger~~~~~~~~~~~~~~~~~~");
        return buildNewTrigger(QuartzName.MINUTE_1);
    }
}
