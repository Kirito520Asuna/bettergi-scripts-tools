package com.cloud_guest.domain.dto;

import com.cloud_guest.aop.validator.ValidCron;
import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
    @JsonView(BasicJsonView.NextCronTimestampALLView.class)
    @NotBlank(message ="key值不能为空" ,groups = {BasicJsonView.NextCronTimestampALLView.class})
    @Schema(description = "key",required = true)
    private String key;
    @JsonView({BasicJsonView.NextCronTimestampALLView.class,BasicJsonView.NextCronTimestampView.class})
    @ValidCron(message = "cron表达式格式错误", groups = {BasicJsonView.NextCronTimestampView.class,BasicJsonView.NextCronTimestampALLView.class})
    @Schema(description = "cron表达式",required = true)
    private String cronExpression;
    @JsonView({BasicJsonView.NextCronTimestampALLView.class,BasicJsonView.NextCronTimestampView.class})
    @NotNull(message = "开始时间戳不能为空",groups = {BasicJsonView.NextCronTimestampView.class,BasicJsonView.NextCronTimestampALLView.class})
    @Schema(description = "开始时间戳",required = true)
    private Long startTimestamp;
    @JsonView({BasicJsonView.NextCronTimestampALLView.class,BasicJsonView.NextCronTimestampView.class})
    @NotNull(message = "结束时间戳不能为空", groups = {BasicJsonView.NextCronTimestampView.class,BasicJsonView.NextCronTimestampALLView.class})
    @Schema(description = "结束时间戳",required = true)
    private Long endTimestamp;
}
