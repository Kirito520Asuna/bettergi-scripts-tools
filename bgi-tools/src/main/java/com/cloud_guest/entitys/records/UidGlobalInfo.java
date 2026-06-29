package com.cloud_guest.entitys.records;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * @Author yan
 * @Date 2026/6/29 22:38:01
 * @Description
 */
@Schema(description = "全局UID自动计划配置")
public record UidGlobalInfo(
        @Schema(description = "UID",required = true)
        @NotBlank(message = "UID不能为空")
        String uid,
        @Schema(description = "是否启用培养计划")
        Boolean cultivate) {
}
