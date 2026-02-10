package com.cloud_guest.utils.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Map;

/**
 * @Author yan
 * @Date 2024/11/2 下午10:13:56
 * @Description
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SingleSignature {
    /**
     * 签名密钥
     */
    // 签名密钥
    private String salt = null;
    /**
     * 请求方法
     */
    // 请求方法
    private String method = null;
    /**
     * 请求URL
     */
    // 请求URL
    private String url = null;
    /**
     * 请求参数
     */
    // 请求参数
    private Map<String, String[]> params = null;
    /**
     * 请求体
     */
    // 请求体
    private Map<String, Object> body = null;
    /**
     * 排除参数
     */
    // 排除参数
    private Collection<String> exCollection = null;
}