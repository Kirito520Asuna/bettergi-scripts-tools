package com.cloud_guest.entitys.records;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Author yan
 * @Date 2026/2/10 13:39:13
 * @Description
 */
@Schema(description = "token信息")
public record TokenVo(@Schema(description = "token名称") String name, @Schema(description = "token值") String value) {
}
