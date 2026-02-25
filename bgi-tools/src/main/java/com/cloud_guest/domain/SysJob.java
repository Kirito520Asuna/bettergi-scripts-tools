package com.cloud_guest.domain;

import cn.hutool.core.util.IdUtil;
import com.cloud_guest.utils.IdUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 定时任务调度表
 */
@Schema(description = "定时任务调度表")
@Data
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SysJob  implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @JsonProperty(value = SysJob.COL_JOB_ID)
    @Schema(description = "任务ID")
    private Long jobId= IdUtils.getNextId();

    /**
     * 任务名称
     */
    @JsonProperty(value = SysJob.COL_JOB_NAME)
    @Schema(description = "任务名称")
    private String jobName;

    /**
     * 任务组名
     */
    @JsonProperty(value = SysJob.COL_JOB_GROUP)
    @Schema(description = "任务组名")
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    @JsonProperty(value = SysJob.COL_INVOKE_TARGET)
    @Schema(description = "调用目标字符串")
    private String invokeTarget;

    /**
     * cron执行表达式
     */
    @JsonProperty(value = SysJob.COL_CRON_EXPRESSION)
    @Schema(description = "cron执行表达式")
    private String cronExpression;

    /**
     * 计划执行错误策略（1立即执行 2执行一次 3放弃执行）
     */
    @JsonProperty(value = SysJob.COL_MISFIRE_POLICY)
    @Schema(description = "计划执行错误策略（1立即执行 2执行一次 3放弃执行）")
    private String misfirePolicy;

    /**
     * 是否并发执行（0允许 1禁止）
     */
    @JsonProperty(value = SysJob.COL_CONCURRENT)
    @Schema(description = "是否并发执行（0允许 1禁止）")
    private String concurrent;

    /**
     * 状态（0正常 1暂停）
     */
    @JsonProperty(value = SysJob.COL_STATUS)
    @Schema(description = "状态（0正常 1暂停）")
    private String status;


    public static final String COL_JOB_ID = "job_id";

    public static final String COL_JOB_NAME = "job_name";

    public static final String COL_JOB_GROUP = "job_group";

    public static final String COL_INVOKE_TARGET = "invoke_target";

    public static final String COL_CRON_EXPRESSION = "cron_expression";

    public static final String COL_MISFIRE_POLICY = "misfire_policy";

    public static final String COL_CONCURRENT = "concurrent";

    public static final String COL_STATUS = "status";

}