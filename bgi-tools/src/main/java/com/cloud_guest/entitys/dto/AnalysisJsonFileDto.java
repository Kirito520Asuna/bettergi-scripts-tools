package com.cloud_guest.entitys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @Author yan
 * @Date 2026/2/6 18:15:42
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisJsonFileDto {
    @NotNull
    @Schema(description = "文件bytes")
    private byte[] bytes;
    @Schema(description = "文件名")
    @NotBlank(message = "文件名不能为空")
    private String filename;
}
