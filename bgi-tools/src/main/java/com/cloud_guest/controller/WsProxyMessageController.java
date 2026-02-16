package com.cloud_guest.controller;

import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.domain.dto.WsProxyDto;
import com.cloud_guest.manager.WsClientManager;
import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author yan
 * @Date 2025/12/31 21:25:51
 * @Description
 */
@Tag(name = "消息")
@RestController
@RequestMapping({"/ws-proxy/message/","/api/ws-proxy/message/","/jwt/ws-proxy/message/" })
public class WsProxyMessageController {

    @Resource
    private WsClientManager wsClientManager;


    @SysLog
    @SneakyThrows
    @Operation(summary = "发送消息")
    @PostMapping("send")
    public String send(@JsonView(BasicJsonView.WsProxyView.class) @Validated @RequestBody WsProxyDto wsProxy) {
        wsClientManager.send(wsProxy);
        return "success";
    }
    @SysLog
    @SneakyThrows
    @Operation(summary = "发送消息v1")
    @PostMapping("send/v1")
    public String sendV1(@JsonView(BasicJsonView.WsProxyViewV1.class)@Validated @RequestBody WsProxyDto wsProxyDto) {
        Map<String, Object> bodyMap = wsProxyDto.getBodyMap();
        String url = wsProxyDto.getUrl();
        wsClientManager.buildUrl(url, wsProxyDto.getToken());
        wsClientManager.send(wsProxyDto.getUrl(), JSONUtil.toJsonStr(bodyMap));
        return "success";
    }
}
