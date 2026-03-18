package com.cloud_guest.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * @Author yan
 * @Date 2026/3/15 22:47:13
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonDto {
    @NotBlank(message = "JSON不能为空")
    String json;
}
