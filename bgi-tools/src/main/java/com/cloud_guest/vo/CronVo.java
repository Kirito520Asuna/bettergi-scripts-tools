package com.cloud_guest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author yan
 * @Date 2026/1/30 9:24:17
 * @Description
 */
@Data @NoArgsConstructor
@AllArgsConstructor @Accessors(chain = true)
public class CronVo {
    @Schema(description = "key")
    private String key;
    @Schema(description = "下次执行时间")
    private Long next;
    @Schema(description = "是否可以执行")
    private boolean ok;
}
