package com.cloud_guest.task.domain;

import com.cloud_guest.task.enums.CronTemplate;
import com.cloud_guest.task.enums.QuartzGroup;
import com.cloud_guest.task.enums.QuartzName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.quartz.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class QuartzObject {
    private Class<Job> clockJobClass;
    private QuartzGroup quartzGroup;
    private QuartzName quartzName;
    private CronTemplate cronTemplate;
    private String value;
    private JobKey jobKey;
    private TriggerKey triggerKey;
    private CronScheduleBuilder scheduleBuilder;
    private JobDetail jobDetail;
    private Trigger trigger;
}
