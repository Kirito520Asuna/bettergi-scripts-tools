package com.cloud_guest.domain.dto;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.domain.WsProxyAccess;
import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @Author yan
 * @Date 2025/12/31 21:30:48
 * @Description
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ws代理类")
public class WsProxyDto {
    @NotBlank
    @JsonView(value = {BasicJsonView.WsProxyViewV1.class, BasicJsonView.WsProxyView.class})
    @Schema(description = "ws地址")
    private String url;
    @JsonView(value = {BasicJsonView.WsProxyViewV1.class, BasicJsonView.WsProxyView.class})
    @Schema(description = "token")
    private String token;
    @Schema(description = "消息体")
    @JsonView(BasicJsonView.WsProxyView.class)
    @JsonProperty("bodyJson")
    private String bodyJson;
    @Schema(description = "消息体")
    @JsonProperty("body")
    @JsonView(BasicJsonView.WsProxyViewV1.class)
    private Map<String, Object> bodyMap;
    @JsonIgnore
    private String uid;
    @PostConstruct
    public void init() {
        if (StrUtil.isNotBlank(bodyJson)&& JSONUtil.isTypeJSON(bodyJson)) {
            Map<String, Object> bean = JSONUtil.toBean(bodyJson, Map.class);
            Object o = bean.get("uid");
            if (o != null) {
                uid = o.toString();
            }
        } else if (bodyMap != null) {
            Object o = bodyMap.get("uid");
            if (o != null) {
                uid = o.toString();
            }
        }
    }
}
