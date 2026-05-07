package com.cloud_guest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author yan
 * @Date 2026/5/8 0:52:05
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogKey {

    String id;
    @Schema(description = "token")
    String token;
    @Schema(description = "应用ID")
    String applicationId;
    @Schema(description = "创建时间")
    LocalDateTime createTime;
    @Schema(description = "过期时间")
    LocalDateTime expireTime;
}
