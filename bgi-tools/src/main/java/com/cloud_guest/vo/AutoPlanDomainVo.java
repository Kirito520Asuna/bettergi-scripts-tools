package com.cloud_guest.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/9 14:19:05
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AutoPlanDomainVo implements Serializable {
    private static final long serialVersionUID = 8997301368952007161L;
    @Schema(description = "执行顺序")
    @JsonProperty("order")
    private Integer order;
    @Schema(description = "执行日期")
    @JsonProperty("days")
    private List<Integer> days;
    @Schema(description = "执行类型")
    @JsonProperty("selectedType")
    private String selectedType;
    @Schema(description = "秘境参数")
    @JsonProperty("autoFight")
    private AutoFightDTO autoFight;

    @NoArgsConstructor
    @Data
    public static class AutoFightDTO {
        @Schema(description = "秘境名称")
        @JsonProperty("domainName")
        private String domainName;
        @Schema(description = "限时/周日 顺序1-3")
        @JsonProperty("sundaySelectedValue")
        private Integer sundaySelectedValue;
        @Schema(description = "队伍")
        @JsonProperty("partyName")
        private String partyName;
        @Schema(description = "秘境轮数")
        @JsonProperty("DomainRoundNum")
        private Integer domainRoundNum;
        @Schema(description = "树脂启用顺序")
        @JsonProperty("physical")
        private List<PhysicalDTO> physical;
    }

    @NoArgsConstructor
    @Data
    public static class PhysicalDTO {
        @JsonProperty("order")
        private Integer order;
        @JsonProperty("name")
        private String name;
        @JsonProperty("open")
        private boolean open;
    }
}
