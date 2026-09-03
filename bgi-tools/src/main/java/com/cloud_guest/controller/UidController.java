package com.cloud_guest.controller;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Token;
import com.cloud_guest.entitys.ClassConvert;
import com.cloud_guest.entitys.Valid;
import com.cloud_guest.entitys.domain.UidInfo;

import com.cloud_guest.entitys.pojo.UidInfoConfig;
import com.cloud_guest.entitys.pojo.UidTeamConfig;
import com.cloud_guest.entitys.records.UidTeam;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.mp.utils.PageUtils;
import com.cloud_guest.result.Result;
import com.cloud_guest.result.page.AbsPage;
import com.cloud_guest.result.page.ResultPage;
import com.cloud_guest.service.UidService;
import com.cloud_guest.service.UidTeamService;
import com.cloud_guest.utils.StrUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UidController implements AbsPage {

    static {
        // 注册 UidInfo 的转换器
        ClassConvert.register(UidInfoConfig.class, UidInfo.class,
                info -> {
                    if (info == null) return null;
                    String uid = info.getUid();
                    String asName = info.getAsName();
                    String username = info.getUsername();
                    String password = info.getPassword();
                    String salt = info.getSalt();

                    String decryptedPassword = null;
                    try {
                        decryptedPassword = StrUtil.isBlankIfStr(password) ? password : info.decryptPassword(password, salt);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return new UidInfo(uid, asName, username, decryptedPassword);
                },
                info -> {
                    if (info == null) return null;
                    return new UidInfoConfig(info.getUid(), info.getAs(), info.getUsername(), info.getPassword());
                }
        );
        // 注册 UidTeam 的转换器
        ClassConvert.register(UidTeam.class, UidTeamConfig.class,
                info -> {
                    if (info == null) return null;
                    Long id = null;
                    boolean notId = StrUtils.isNotBlank(info.id());
                    if (notId) {
                        id = Long.parseLong(info.id());
                    }
                    return new UidTeamConfig(id, info.uid(), info.team(), info.type());

                },
                info -> {
                    if (info == null) return null;
                    String id = (info.getId() != null ? String.valueOf(info.getId()) : null);
                    return new UidTeam(id, info.getUid(), info.getTeam(), info.getTeamType());
                }
        );

        // 注册 UidTeam 的校验器，lambda 参数类型会被编译器推断为 UidTeam
        Valid.register(UidTeam.class, uidTeam -> {
            String team = uidTeam.team();
            String uid = uidTeam.uid();
            String type = uidTeam.type();
            if (team == null || team.isBlank()) {
                throw new GlobalException("team 不能为空");
            }
            if (uid == null || uid.isBlank()) {
                throw new GlobalException("uid 不能为空");
            }
            if (type == null || type.isBlank()) {
                throw new GlobalException("type 不能为空");
            }
        });
    }

    @Resource
    private UidService uidService;
    @Resource
    private UidTeamService uidTeamService;

    @SysLog
    @Token
    @Operation(summary = "查询分页uid映射")
    @GetMapping("page")
    public Result<ResultPage<UidInfo>> page(@Schema(description = "页码") @RequestParam long pageNumber,
                                            @Schema(description = "每页数量") @RequestParam long pageSize) {
        PageUtils.startPage(pageNumber, pageSize);
        List<UidInfo> uidAll = uidService.list()
                .stream()
                .map(info -> {
                    UidInfo convert = ClassConvert.convert(UidInfoConfig.class, UidInfo.class, info);
                    convert.setPassword(null);
                    return convert;
                }).toList();
        return Result.ok(listToPage(uidAll));
    }

    @SysLog
    @Token
    @Operation(summary = "查询uid映射")
    @GetMapping("info")
    public Result<UidInfo> getUid(@RequestParam String uid) {
        return Result.ok(Optional.ofNullable(uidService.find(uid))
                .map(info -> ClassConvert.convert(UidInfoConfig.class, UidInfo.class, info))
                .orElse(null));
    }

    @SysLog
    @Token
    @Operation(summary = "新增uid映射")
    @PostMapping("info")
    public Result uid(@Validated @RequestBody UidInfo uidInfo) {
        UidInfoConfig config = ClassConvert.convert(UidInfo.class, UidInfoConfig.class, uidInfo);
        uidService.saveOrUpdate(config);
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


    @SysLog
    @Operation(summary = "[Team]-查询分页-uid映射队伍配置")
    @GetMapping("team/page")
    public Result<ResultPage<UidTeam>> teamList(
            @Schema(description = "ID") @RequestParam(required = false) String id,
            @Schema(description = "UID") @RequestParam(required = false) String uid,
            @Schema(description = "类型") @RequestParam(required = false) String type,
            @Schema(description = "页码") @RequestParam long pageNumber,
            @Schema(description = "每页数量") @RequestParam long pageSize
    ) {
        PageUtils.startPage(pageNumber, pageSize);
        List<UidTeam> uidTeam = uidTeamService.searchList(id, uid, type)
                .stream()
                .map(info -> ClassConvert.convert(UidTeamConfig.class, UidTeam.class, info))
                .toList();
        return Result.ok(listToPage(uidTeam));
    }

    @SysLog
    @Operation(summary = "[Team]-查询指定-uid映射队伍配置")
    @GetMapping("team")
    public Result<UidTeam> team(@Schema(description = "UID") @Validated @NotBlank @RequestParam String uid,
                                @Schema(description = "类型") @Validated @NotBlank @RequestParam String type) {
        return Result.ok(Optional.ofNullable(uidTeamService.searchOne(uid, type))
                .map(info -> ClassConvert.convert(UidTeamConfig.class, UidTeam.class, info))
                .orElse(null));
    }

    @SysLog
    @Operation(summary = "[Team]-查询指定-uid映射队伍配置")
    @GetMapping("team/info")
    public Result<UidTeam> teamInfo(@Schema(description = "ID") @Validated @NotBlank @RequestParam String id) {
        return Result.ok(Optional.ofNullable(uidTeamService.getById(Long.parseLong(id)))
                .map(info -> ClassConvert.convert(UidTeamConfig.class, UidTeam.class, info))
                .orElse(null));
    }

    @SysLog
    @Token
    @Operation(summary = "[Team]-保存/修改-uid映射队伍配置")
    @PostMapping("team")
    public Result<UidTeam> team(@RequestBody UidTeam uidTeam) {
        Valid.validate(UidTeam.class, uidTeam);
        UidTeamConfig config = ClassConvert.convert(UidTeam.class, UidTeamConfig.class, uidTeam);
        return Result.ok(Optional.ofNullable(uidTeamService.saveOrUpdateById(config))
                .map(info -> ClassConvert.convert(UidTeamConfig.class, UidTeam.class, info))
                .orElse(null));
    }

    @SysLog
    @Token
    @Operation(summary = "[Team]-批量移除-uid映射队伍配置")
    @DeleteMapping("team")
    public Result<Boolean> team(@Schema(description = "UID") @Validated @NotBlank @RequestParam(value = "ids") String idStr) {
        List<Long> ids = Arrays.stream(StrUtils.replace(idStr, "[^0-9,]", "").split(",")).map(Long::parseLong).toList();
        return Result.ok(uidTeamService.removeBatchByIds(ids));
    }
}
