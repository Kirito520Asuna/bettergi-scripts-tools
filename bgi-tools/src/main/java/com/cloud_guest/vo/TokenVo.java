package com.cloud_guest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/2/10 13:39:13
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenVo {
    @Schema(description = "token名称")
    private String name;
    @Schema(description = "token值")
    private String value;
}
