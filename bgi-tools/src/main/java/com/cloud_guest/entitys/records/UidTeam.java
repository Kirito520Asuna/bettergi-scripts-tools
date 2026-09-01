package com.cloud_guest.entitys.records;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Author yan
 * @Date 2026/9/1 16:47:00
 * @Description
 */
@Schema(description = "Uid队伍信息")
public record UidTeam(@Schema(description = "ID") String id,
                      @Schema(description = "UID",required = true) String uid,
                      @Schema(description = "队伍",required = true) String team,
                      @Schema(description = "类型",required = true) String type) {

}
