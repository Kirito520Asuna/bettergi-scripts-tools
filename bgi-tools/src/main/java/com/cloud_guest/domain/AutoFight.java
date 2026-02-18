package com.cloud_guest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/18 0:52:32
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoFight {
    //@Schema(description = "国家地区")
    //@JsonProperty("country")
    //public String country;
    @Schema(description = "秘境名称")
    @JsonProperty("domainName")
    private String domainName;
    @Schema(description = "限时/周日 顺序1-3")
    @JsonProperty("sundaySelectedValue")
    private Integer sundaySelectedValue;
    @Schema(description = "限时/周日 材料 名称")
    private String sundaySelectedName;
    @Schema(description = "队伍")
    @JsonProperty("partyName")
    private String partyName;
    @Schema(description = "秘境轮数")
    @JsonProperty("domainRoundNum")
    private Integer domainRoundNum;
    @Schema(description = "树脂启用顺序")
    @JsonProperty("physical")
    private List<Physical> physical;
}
