package com.cloud_guest.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Login;
import com.cloud_guest.domain.WsProxyAccess;
import com.cloud_guest.domain.dto.WsProxyDto;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.manager.WsClientManager;
import com.cloud_guest.properties.auth.AuthProperties;
import com.cloud_guest.properties.check.TokenProperties;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.WsProxyService;
import com.cloud_guest.utils.ServletUtil;
import com.cloud_guest.utils.TokenUtil;
import com.cloud_guest.utils.object.ObjectUtils;
import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cloud_guest.result.Result.ok;

/**
 * @Author yan
 * @Date 2025/12/31 21:25:51
 * @Description
 */
@Tag(name = "ws代理", description = "ws代理")
@RestController
@RequestMapping({"/ws-proxy/", "/api/ws-proxy/", "/jwt/ws-proxy/"})
public class WsProxyController {

    @Resource
    private WsClientManager wsClientManager;
    @Resource
    private WsProxyService wsProxyService;

    @SysLog
    @SneakyThrows
    @Operation(summary = "发送消息")
    @PostMapping("message/send")
    public Result send(@JsonView(BasicJsonView.WsProxyView.class) @Validated @RequestBody WsProxyDto wsProxy) {
        if (!TokenUtil.checkToken()) {
            String uid = wsProxy.getUid();
            if (StrUtil.isBlank(uid)) {
                throw new GlobalException("请输入 UID");
            }
            WsProxyAccess proxyAccess = wsProxyService.find(uid);
            if (proxyAccess == null) {
                throw new GlobalException("UID 未授权");
            }
        }
        wsClientManager.send(wsProxy);
        return Result.ok();
    }

    @SysLog
    @SneakyThrows
    @Operation(summary = "发送消息v1")
    @PostMapping("message/send/v1")
    public Result sendV1(@JsonView(BasicJsonView.WsProxyViewV1.class) @Validated @RequestBody WsProxyDto wsProxyDto) {
        if (!TokenUtil.checkToken()) {
            String uid = wsProxyDto.getUid();
            if (StrUtil.isBlank(uid)) {
                throw new GlobalException("请输入 UID");
            }
            WsProxyAccess proxyAccess = wsProxyService.find(uid);
            if (proxyAccess == null) {
                throw new GlobalException("UID 未授权");
            }
        }
        Map<String, Object> bodyMap = wsProxyDto.getBodyMap();
        String url = wsProxyDto.getUrl();
        wsClientManager.buildUrl(url, wsProxyDto.getToken());
        wsClientManager.send(wsProxyDto.getUrl(), JSONUtil.toJsonStr(bodyMap));
        return Result.ok();
    }

    @SysLog
    @Operation(summary = "查询授权全部UID")
    @GetMapping("access/uid/all")
    public Result<List<String>> accessUidALL() {
        List<String> uidList = wsProxyService.findUidAll();
        uidList = uidList.stream().map(uid -> uid.substring(uid.lastIndexOf(":") + 1)).toList();
        return ok(uidList);
    }

    @SysLog
    @Operation(summary = "查询授权全部")
    @GetMapping("access/all")
    public Result<List<WsProxyAccess>> accessALL() {
        List<WsProxyAccess> proxyAccessList = wsProxyService.findAll();
        return ok(proxyAccessList);
    }

    @SysLog
    @Operation(summary = "查询授权")
    @GetMapping("access")
    public Result<WsProxyAccess> access(@RequestParam String uid) {
        WsProxyAccess proxyAccess = wsProxyService.find(uid);
        return ok(proxyAccess);
    }

    @Login
    @SysLog
    @Operation(summary = "保存授权")
    @PostMapping("access")
    public Result access(@RequestBody WsProxyAccess wsProxyAccess) {
        wsProxyService.save(wsProxyAccess.getUid(), JSONUtil.toJsonStr(wsProxyAccess));
        return ok();
    }

    @Login
    @SysLog
    @Operation(summary = "移除授权")
    @DeleteMapping("access")
    public Result accessRemove(@RequestParam String uids) {
        List<String> uidList = new ArrayList<>();
        if (StrUtil.isNotBlank(uids)) {
            Arrays.stream(uids.split(",")).forEach(uidList::add);
        }
        wsProxyService.delList(uidList);
        return ok();
    }
}
