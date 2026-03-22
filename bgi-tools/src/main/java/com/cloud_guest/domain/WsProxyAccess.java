package com.cloud_guest.domain;

import com.cloud_guest.domain.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/3/22 17:31:31
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsProxyAccess {
    @Schema(description = "操作类型 私聊/群聊")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ActionType action;
    @Schema(description = "ws地址")
    @JsonProperty("ws_url")
    private String url;
    @Schema(description = "ws代理地址")
    @JsonProperty("ws_proxy_url")
    private String proxyUrl;
    @Schema(description = "授权token")
    @JsonProperty("ws_token")
    private String token;
    @Schema(description = "at列表")
    @JsonProperty("at_list")
    private String atList;
    @Schema(description = "用户id")
    @JsonProperty("user_id")
    private String userId;
    @Schema(description = "群id")
    @JsonProperty("group_id")
    private String groupId;
    @Schema(description = "uid")
    private String uid;
}
