package com.cloud_guest.controller;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Token;
import com.cloud_guest.domain.UidInfo;
import com.cloud_guest.pojo.UidInfoConfig;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.UidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @Author yan
 * @Date 2026/3/30 17:22:22
 * @Description
 */
@Slf4j
@Tag(name = "uid映射服务")
@RestController
@RequestMapping(value = {"/uid/", "/api/uid/", "/jwt/uid/"})
public class UidController {
    @Resource
    private UidService uidService;
    @SysLog
    @Operation(summary = "查询全部uid映射")
    @GetMapping("all")
    public Result<List<UidInfo>> all() {
        List<UidInfo> uidAll = uidService.findUidAll().stream().map(UidInfoConfig::toUidInfo).toList();
        return Result.ok(uidAll);
    }
    @SysLog
    @Operation(summary = "查询uid映射")
    @GetMapping("info")
    public Result<UidInfo> getUid(@RequestParam String uid) {
        UidInfo uidInfo = Optional.ofNullable(uidService.find(uid))
                .map(UidInfoConfig::toUidInfo)
                .orElse(null);   // 返回一个空的 UidInfo 对象（确保 UidInfo 有无参构造）
        return Result.ok(uidInfo);
    }
    @SysLog
    @Token
    @Operation(summary = "新增uid映射")
    @PostMapping("info")
    public Result uid(@Validated @RequestBody UidInfo uidInfo) {
        uidService.saveOrUpdate(uidInfo.toConfig());
        return Result.ok();
    }
    @SysLog
    @Token
    @Operation(summary = "移除uid映射")
    @DeleteMapping("info")
    public Result remove(@NotBlank @RequestParam String ids) {
        List<String> uidList = Arrays.stream(ids.split(",")).toList();
        uidService.removeList(uidList);
        return Result.ok();
    }
}
