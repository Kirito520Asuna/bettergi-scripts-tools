package com.cloud_guest.entitys.common.auto_plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/7 13:57:44
 * @Description
 */
@Data @Schema(description = "幽境")
@NoArgsConstructor
@AllArgsConstructor
public class AutoStygianOnslaught {
    @JsonProperty("physical")
    @Schema(description = "树脂启用顺序")
    private List<Physical> physical;
    @Schema(description = "指定使用树脂")
    @JsonProperty("specifyResinUse")
    private boolean specifyResinUse;
    @Schema(description = "战场[1,2,3]")
    @JsonProperty("bossNum")
    private Integer bossNum;
    @Schema(description = "战斗队伍")
    @JsonProperty("fightTeamName")
    private String fightTeamName;
}
