package com.cloud_guest.swagger.properties.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/4/1 13:32:14
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiHeader {
    String name;
    String value;
}
