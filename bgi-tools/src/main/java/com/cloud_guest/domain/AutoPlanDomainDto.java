package com.cloud_guest.domain;

import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Author yan
 * @Date 2026/2/8 15:55:21
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoPlanDomainDto implements Serializable {
    private static final long serialVersionUID = 4630197595885404176L;
    @Schema(description = "uid")
    @NotBlank(groups = {BasicJsonView.AutoPlanDomainView.class})
    @JsonView(value = {BasicJsonView.AutoPlanDomainView.class})
    private String uid;
    @Schema(description = "json")
    @NotNull(groups = {BasicJsonView.AutoPlanDomainView.class,BasicJsonView.AutoPlanDomainALLView.class})
    @JsonView(value = {BasicJsonView.AutoPlanDomainView.class,BasicJsonView.AutoPlanDomainALLView.class})
    private String json;
}
