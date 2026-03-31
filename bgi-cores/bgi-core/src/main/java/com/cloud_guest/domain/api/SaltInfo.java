package com.cloud_guest.domain.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author yan
 * @Date 2024/5/20 0020 15:49
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor @Accessors(chain = true)
public class SaltInfo {
    /**
     * 盐值
     */
    private String salt;
    /**
     * 盐值对应服务名
     */
    private String serviceName;
}
