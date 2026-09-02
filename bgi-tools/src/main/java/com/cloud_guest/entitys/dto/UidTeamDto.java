package com.cloud_guest.entitys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/9/2 12:27:32
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UidTeamDto {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "UID", required = true)
    private String uid;
    @Schema(description = "队伍", required = true)
    private String team;
    @Schema(description = "类型", required = true)
    private String type;

}
