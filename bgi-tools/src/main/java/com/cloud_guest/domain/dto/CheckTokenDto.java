package com.cloud_guest.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Author yan
 * @Date 2026/2/23 16:52:58
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "授权token")
public class CheckTokenDto {
    @Schema(description = "token名称")
    @NotBlank(message = "token名称不能为空")
    private String tokenName;
    @Schema(description = "token值")
    @NotBlank(message = "token值不能为空")
    private String tokenValue;
}
