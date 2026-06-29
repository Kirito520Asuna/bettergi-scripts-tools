package com.cloud_guest.entitys.common.auto_plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/2/18 0:51:34
 * @Description
 */
@Data @Schema(description = "体力")
@NoArgsConstructor
@AllArgsConstructor
public class Physical {
    @Schema(description = "顺序")
    @JsonProperty("order")
    private Integer order;
    @Schema(description = "名称[浓缩树脂/原粹树脂/须臾树脂/脆弱树脂]")
    @JsonProperty("name")
    private String name;
    @Schema(description = "是否启用")
    @JsonProperty("open")
    private boolean open;
    @Schema(description = "数量")
    @JsonProperty("count")
    private long count;
}
