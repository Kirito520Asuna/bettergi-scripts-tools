package com.cloud_guest.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Login;
import com.cloud_guest.aop.security.Token;
import com.cloud_guest.entitys.ClassConvert;
import com.cloud_guest.entitys.Valid;
import com.cloud_guest.entitys.common.enums.ActionType;
import com.cloud_guest.entitys.records.WsProxyAccess;
import com.cloud_guest.entitys.dto.WsProxyDto;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.mp.utils.PageUtils;
import com.cloud_guest.result.page.AbsPage;
import com.cloud_guest.result.page.ResultPage;
import com.cloud_guest.websocket.WsClientManager;
import com.cloud_guest.entitys.pojo.WsProxyAccessConfig;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.WsProxyService;
import com.cloud_guest.utils.TokenUtil;
import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.cloud_guest.result.Result.ok;

/**
 * @Author yan
 * @Date 2025/12/31 21:25:51
 * @Description
 */
@Tag(name = "ws代理", description = "ws代理")
@RestController
@RequestMapping({"/ws-proxy/", "/api/ws-proxy/", "/jwt/ws-proxy/"})
public class WsProxyController implements AbsPage {
    static {
        ClassConvert.register(WsProxyAccess.class, WsProxyAccessConfig.class,
                info -> {
            if (info == null){
                return null;
            }
            ActionType action = info.action();
            String url = info.url();
            String proxyUrl = info.proxyUrl();
            String token = info.token();
            String atList = info.atList();
            String userId = info.userId();
            String groupId = info.groupId();
            String uid = info.uid();
            WsProxyAccessConfig config = new WsProxyAccessConfig();
            config.setAction(action);
            config.setUrl(url);
            config.setProxyUrl(proxyUrl);
            config.setToken(token);
            config.setAtList(atList);
            config.setUserId(userId);
            config.setGroupId(groupId);
            config.setUid(uid);
            return config;
        },
                info -> {
            if (info == null){
                return null;
            }
            ActionType action = info.getAction();
            String url = info.getUrl();
            String proxyUrl = info.getProxyUrl();
            String token = info.getToken();
            String atList = info.getAtList();
            String userId = info.getUserId();
            String groupId = info.getGroupId();
            String uid = info.getUid();
            return new WsProxyAccess(action, url, proxyUrl, token, atList, userId, groupId, uid);
        });

        Valid.register(WsProxyDto.class, info -> {
            if (!TokenUtil.checkToken()) {
                String uid = info.getUid();
                if (StrUtil.isBlank(uid)) {
                    throw new GlobalException("请输入 UID");
                }
                WsProxyAccessConfig accessConfig = SpringUtil.getBean(WsProxyService.class).getById(uid);
                if (accessConfig == null) {
                    throw new GlobalException("UID 未授权");
                }
            }
        });
    }

    @Resource
    private WsClientManager wsClientManager;
    @Resource
    private WsProxyService wsProxyService;

    @SysLog
    @SneakyThrows
    @Operation(summary = "发送消息")
    @PostMapping("message/send")
    public Result send(@JsonView(BasicJsonView.WsProxyView.class) @Validated @RequestBody WsProxyDto wsProxy) {
        Valid.validate(WsProxyDto.class, wsProxy);
        wsClientManager.send(wsProxy);
        return Result.ok();
    }

    @SysLog
    @SneakyThrows
    @Operation(summary = "发送消息v1")
    @PostMapping("message/send/v1")
    public Result sendV1(@JsonView(BasicJsonView.WsProxyViewV1.class) @Validated @RequestBody WsProxyDto wsProxyDto) {
        Valid.validate(WsProxyDto.class, wsProxyDto);
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
        return ok(uidList);
    }

    @Token
    @SysLog
    @Operation(summary = "查询授权全部")
    @GetMapping("access/page")
    public Result<ResultPage<WsProxyAccess>> accessPage(
            @RequestParam(required = false) String uid,
            @RequestParam long pageNumber,
            @RequestParam long pageSize) {
        PageUtils.startPage(pageNumber, pageSize);
        List<WsProxyAccess> list = wsProxyService.searchList(uid)
                .stream()
                .map(info -> ClassConvert.convert(WsProxyAccessConfig.class, WsProxyAccess.class, info))
                .toList();
        return ok(listToPage(list));
    }

    @Token
    @SysLog
    @Operation(summary = "查询授权")
    @GetMapping("access")
    public Result<WsProxyAccess> access(@RequestParam String uid) {
        WsProxyAccessConfig accessConfig = wsProxyService.getById(uid);
        WsProxyAccess record =  ClassConvert.convert(WsProxyAccessConfig.class, WsProxyAccess.class, accessConfig);
        return ok(record);
    }

    @Login
    @SysLog
    @Operation(summary = "保存授权")
    @PostMapping("access")
    public Result access(@RequestBody WsProxyAccess wsProxyAccess) {
        WsProxyAccessConfig config = ClassConvert.convert(WsProxyAccess.class, WsProxyAccessConfig.class, wsProxyAccess);
        wsProxyService.saveOrUpdate(config);
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
        wsProxyService.removeBatchByIds(uidList);
        return ok();
    }
}
