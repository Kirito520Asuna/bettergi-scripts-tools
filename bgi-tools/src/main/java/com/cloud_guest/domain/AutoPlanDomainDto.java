package com.cloud_guest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author yan
 * @Date 2026/2/8 15:55:21
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoPlanDomainDto {
    @Schema(description = "uid")
    @NotBlank
    private String uid;
    @Schema(description = "json")
    @NotNull
    private String json;
}
