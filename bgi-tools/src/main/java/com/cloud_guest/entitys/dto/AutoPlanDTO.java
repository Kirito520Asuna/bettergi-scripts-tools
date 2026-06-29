package com.cloud_guest.entitys.dto;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.aop.validator.NotEmptyList;
import com.cloud_guest.entitys.common.auto_plan.AutoFight;
import com.cloud_guest.entitys.common.auto_plan.AutoLeyLineOutcrop;
import com.cloud_guest.entitys.common.auto_plan.AutoPlan;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.entitys.pojo.AutoPlanConfig;
import com.cloud_guest.utils.object.ObjectUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

    public void checkValid() {
        List<String> runTypes = Arrays.asList("秘境", "地脉","幽境","Boss");
        for (AutoPlan autoPlan : this.autoPlanList) {
            if (!runTypes.contains(autoPlan.getRunType())) {
                String runTypesStr = runTypes.stream().collect(Collectors.joining(","));
                throw new GlobalException("runType参数错误,支持类型:" + runTypesStr);
            } else if (ObjectUtils.equals(runTypes.get(0), autoPlan.getRunType())) {
                //秘境效益
                AutoFight autoFight = autoPlan.getAutoFight();
                String domainName = autoFight.getDomainName();
                if (StrUtil.isBlank(domainName)) {
                    throw new GlobalException("秘境名称不能为空");
                }
            } else if (ObjectUtils.equals(runTypes.get(1), autoPlan.getRunType())) {
                List<String> leyLineOutcropTypes = Arrays.asList("启示之花", "藏金之花");
                //地脉效益
                AutoLeyLineOutcrop autoLeyLineOutcrop = autoPlan.getAutoLeyLineOutcrop();
                String country = autoLeyLineOutcrop.getCountry();
                String leyLineOutcropType = autoLeyLineOutcrop.getLeyLineOutcropType();
                if (!leyLineOutcropTypes.contains(leyLineOutcropType)) {
                    throw new GlobalException("地脉类型错误,支持类型:" + leyLineOutcropTypes.stream().collect(Collectors.joining(",")));
                }
                if (StrUtil.isBlank(country)) {
                    throw new GlobalException("国家地区不能为空");
                }
            }
        }
    }


    public List<AutoPlanConfig> toConfigList() {
        List<AutoPlanConfig> list = autoPlanList.stream().map(autoPlan -> {
            AutoPlanConfig config = autoPlan.toConfig();
            config.setUid(uid);
            if(Boolean.TRUE.equals(cultivate)){
                config.setCultivate(Boolean.TRUE);
            }
            return config;
        }).collect(Collectors.toList());
        return list;
    }
}
