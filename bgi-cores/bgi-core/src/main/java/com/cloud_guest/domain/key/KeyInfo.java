package com.cloud_guest.domain.key;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author yan
 * @Date 2026/3/31 19:38:13
 * @Description
 */
@Data
@Schema(description = "密钥信息")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KeyInfo {
    @Schema(description = "密钥ID")
    private String id;
    @Schema(description = "公钥")
    private String publicKeyBase64;
    @Schema(description = "私钥")
    private String privateKeyBase64;
    @Schema(description = "创建时间")
    private Long createTime;
    @Schema(description = "有效时间间隔，默认3分钟")
    private Long validTimeInterval=3*60*1000L;
}
