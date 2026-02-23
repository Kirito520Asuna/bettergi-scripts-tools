package com.cloud_guest.domain.dto;

import com.cloud_guest.aop.validator.NotEmptyList;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/23 21:02:15
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {
    @NotEmptyList
    @Schema(description = "应用ID列表")
    private List<String> ids;
}
