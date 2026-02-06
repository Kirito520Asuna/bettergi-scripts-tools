package com.cloud_guest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/2/6 18:27:11
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cache<T extends Object> {
    //类型
    private String type;
    //data
    private T data;
}
