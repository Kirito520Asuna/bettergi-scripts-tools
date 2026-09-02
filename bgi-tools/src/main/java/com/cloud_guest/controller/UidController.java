package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Token;
import com.cloud_guest.entitys.Valid;
import com.cloud_guest.entitys.domain.UidInfo;
import com.cloud_guest.entitys.dto.UidTeamDto;
import com.cloud_guest.entitys.pojo.UidInfoConfig;
import com.cloud_guest.entitys.pojo.UidTeamConfig;
import com.cloud_guest.entitys.records.UidTeam;
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
    @Resource
    private UidService uidService;
    @Resource
    private UidTeamService uidTeamService;

    @SysLog
    @Token
    @Operation(summary = "查询全部uid映射")
    @GetMapping("all")
    public Result<List<UidInfo>> all() {
        List<UidInfo> uidAll = uidService.findUidAll().stream().map(UidInfoConfig::toUidInfo).map(o -> {
            o.setPassword(null);
            return o;
        }).toList();
        return Result.ok(uidAll);
    }

    @SysLog
    @Token
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


    @SysLog
    @Operation(summary = "[Team]-查询分页-uid映射队伍配置")
    @GetMapping("team/page")
    public Result<ResultPage<UidTeam>> teamList(
            @Schema(description = "ID")  @RequestParam(required = false) String id,
            @Schema(description = "UID")  @RequestParam(required = false) String uid,
            @Schema(description = "类型")  @RequestParam(required = false) String type,
            @Schema(description = "页码")  @RequestParam long page,
            @Schema(description = "每页数量")  @RequestParam long size
    ){
        PageUtils.startPage(page, size);
        List<UidTeam> uidTeam = uidTeamService.searchList(id,uid,type).stream().map(UidTeamConfig::toRecord).toList();
        return Result.ok(listToPage(uidTeam));
    }

    @SysLog
    @Operation(summary = "[Team]-查询指定-uid映射队伍配置")
    @GetMapping("team")
    public Result<UidTeam> team(@Schema(description = "UID") @Validated @NotBlank @RequestParam String uid,
                                @Schema(description = "类型") @Validated @NotBlank @RequestParam String type){
        UidTeamConfig uidTeamConfig = uidTeamService.searchOne(uid, type);
        UidTeam record = uidTeamConfig != null ? uidTeamConfig.toRecord() : null;
        return Result.ok(record);
    }

    @SysLog
    @Operation(summary = "[Team]-查询指定-uid映射队伍配置")
    @GetMapping("team/info")
    public Result<UidTeam> teamInfo(@Schema(description = "ID") @Validated @NotBlank @RequestParam String id){
        UidTeamConfig uidTeamConfig = uidTeamService.getById(Long.parseLong(id));
        UidTeam record = uidTeamConfig != null ? uidTeamConfig.toRecord() : null;
        return Result.ok(record);
    }

    @SysLog
    @Token
    @Operation(summary = "[Team]-保存/修改-uid映射队伍配置")
    @PostMapping("team")
    public Result<UidTeam> team(@RequestBody UidTeam uidTeam){
        Valid.validate(UidTeam.class, uidTeam);

        UidTeamConfig config = new UidTeamConfig();
        boolean notId = StrUtils.isNotBlank(uidTeam.id());
        if (notId){
            config.setId(Long.parseLong(uidTeam.id()));
        }
        config.setUid(uidTeam.uid())
                .setTeam(uidTeam.team())
                .setTeamType(uidTeam.type());

        return Result.ok(uidTeamService.saveOrUpdateById(config).toRecord());
    }
    @SysLog
    @Token
    @Operation(summary = "[Team]-批量移除-uid映射队伍配置")
    @DeleteMapping("team")
    public Result<Boolean> team(@Schema(description = "UID") @Validated @NotBlank @RequestParam(value = "ids") String idStr){
        List<Long> ids = Arrays.stream(StrUtils.replace(idStr, "[^0-9,]", "").split(",")).map(Long::parseLong).toList();
        return Result.ok(uidTeamService.removeBatchByIds(ids));
    }
}
