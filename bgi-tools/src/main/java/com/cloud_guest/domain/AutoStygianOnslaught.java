package com.cloud_guest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/7 13:57:44
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoStygianOnslaught {
    @JsonProperty("physical")
    private List<Physical> physical;
    @JsonProperty("specifyResinUse")
    private boolean specifyResinUse;
    @JsonProperty("bossNum")
    private Integer bossNum;
    @JsonProperty("fightTeamName")
    private String fightTeamName;
}
