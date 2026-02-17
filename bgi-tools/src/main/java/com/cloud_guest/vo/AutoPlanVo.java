package com.cloud_guest.vo;

import com.cloud_guest.domain.AutoFight;
import com.cloud_guest.domain.AutoLeyLineOutcrop;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/9 14:19:05
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AutoPlanVo implements Serializable {
    private static final long serialVersionUID = 8997301368952007161L;
    @Schema(description = "执行顺序")
    @JsonProperty("order")
    private Integer order;
    @Schema(description = "执行日期")
    @JsonProperty("days")
    private List<Integer> days;
    @JsonProperty("dayName")
    private String dayName;
    //@Schema(description = "执行类型(展示用)")
    @JsonProperty("selectedType")
    private String selectedType;
    @Schema(description = "执行类型(秘境|地脉)")
    @JsonProperty("runType")
    private String runType;
    @Schema(description = "秘境参数")
    @JsonProperty("autoFight")
    private AutoFight autoFight;
    @Schema(description = "地脉参数")
    @JsonProperty("autoLeyLineOutcrop")
    private AutoLeyLineOutcrop autoLeyLineOutcrop;

}
