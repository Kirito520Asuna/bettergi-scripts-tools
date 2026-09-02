package  com.cloud_guest.entitys.records;

import com.cloud_guest.entitys.common.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Author yan
 * @Date 2026/9/2 20:55:47
 * @Description
 */
public record WsProxyAccess(@Schema(description = "操作类型 私聊/群聊")
                            @JsonFormat(shape = JsonFormat.Shape.STRING)
                            ActionType action,
                            @Schema(description = "ws地址")
                            @JsonProperty("ws_url")
                            String url,
                            @Schema(description = "ws代理地址")
                            @JsonProperty("ws_proxy_url")
                            String proxyUrl,
                            @Schema(description = "授权token")
                            @JsonProperty("ws_token")
                            String token,
                            @Schema(description = "at列表")
                            @JsonProperty("at_list")
                            String atList,
                            @Schema(description = "用户id")
                            @JsonProperty("user_id")
                            String userId,
                            @Schema(description = "群id")
                            @JsonProperty("group_id")
                            String groupId,
                            @Schema(description = "uid")
                            String uid) {
}
