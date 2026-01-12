package com.cloud_guest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author yan
 * @Date 2026/1/12 17:40:15
 * @Description
 */
@Data @Schema(description = "cron解析类")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CronDto {
    @Schema(description = "cron表达式")
    private String cronExpression;
    @Schema(description = "开始时间戳")
    private long startTimestamp;
    @Schema(description = "结束时间戳")
    private long endTimestamp;
}
