package com.cloud_guest.vo;

import com.cloud_guest.domain.key.KeyInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author yan
 * @Date 2026/3/31 19:42:37
 * @Description
 */
@Data
@Schema(description = "密钥信息")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KeyInfoVo {
    @Schema(description = "密钥ID")
    private String id;
    @Schema(description = "加密过的公钥")
    private String publicKeyEncryption;
    @Schema(description = "创建时间")
    private Long createTime;
    @Schema(description = "有效时间间隔，默认3分钟")
    private Long validTimeInterval = 3 * 60 * 1000L;

    /**
     * 构建KeyInfoVo对象的方法
     * 将KeyInfo对象的数据转换并设置到KeyInfoVo对象中
     *
     * @param keyInfo 包含密钥信息的原始对象
     * @return 返回一个已设置好所有属性的KeyInfoVo对象
     */
    public static KeyInfoVo build(KeyInfo keyInfo,String publicKeyEncryption) {
        // 使用链式调用创建KeyInfoVo对象并设置其属性
        return new KeyInfoVo()
                // 设置密钥ID
                .setId(keyInfo.getId())
                // 设置Base64编码的公钥
                .setPublicKeyEncryption(publicKeyEncryption)
                // 设置创建时间
                .setCreateTime(keyInfo.getCreateTime())
                // 设置有效时间间隔
                .setValidTimeInterval(keyInfo.getValidTimeInterval());
    }
}
