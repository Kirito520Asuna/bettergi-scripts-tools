package com.cloud_guest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/2/18 0:51:34
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Physical {
    @JsonProperty("order")
    private Integer order;
    @JsonProperty("name")
    private String name;
    @JsonProperty("open")
    private boolean open;
}
