package com.cloud_guest.entitys.dto;

import com.cloud_guest.aop.validator.NotEmptyList;
import com.cloud_guest.entitys.ClassConvert;
import com.cloud_guest.entitys.common.auto_plan.AutoPlan;
import com.cloud_guest.entitys.pojo.AutoPlanConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/9 14:19:05
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AutoPlanDTO implements Serializable {
    private static final long serialVersionUID = 8997301368952007161L;
    @Schema(description = "uid")
    @NotBlank(message = "uid不能为空")
    private String uid;
    @Schema(description = "cultivate 是培养计划")
    private Boolean cultivate;
    @Schema(description = "先移除原有培养计划再新增")
    private Boolean removeCultivate;
    @Schema(description = "自动计划列表")
    @NotEmptyList(message = "自动计划列表不能为空")
    private List<AutoPlan> autoPlanList = new ArrayList<>();


    public List<AutoPlanConfig> toConfigList() {
        List<AutoPlanConfig> list = autoPlanList.stream().map(autoPlan -> {
            AutoPlanConfig config = ClassConvert.convert(AutoPlan.class, AutoPlanConfig.class, autoPlan);
            config.setUid(uid);
            if(Boolean.TRUE.equals(cultivate)){
                config.setCultivate(Boolean.TRUE);
            }
            return config;
        }).collect(Collectors.toList());
        return list;
    }
}
