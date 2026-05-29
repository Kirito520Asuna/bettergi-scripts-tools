package com.cloud_guest.entitys.vo;

import com.cloud_guest.entitys.common.auto_plan.AutoPlan;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author yan
 * @Date 2026/2/9 14:19:05
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data  @Accessors(chain = true)
public class AutoPlanVo extends AutoPlan implements Serializable {
    private static final long serialVersionUID = 8997301368952007161L;
    @JsonIgnore
    private String noting;
}
