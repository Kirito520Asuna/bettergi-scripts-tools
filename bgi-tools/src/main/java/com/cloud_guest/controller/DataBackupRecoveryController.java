package com.cloud_guest.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.entitys.ClassConvert;
import com.cloud_guest.entitys.domain.BackUp;
import com.cloud_guest.entitys.dto.JsonDto;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.mp.utils.PageUtils;
import com.cloud_guest.entitys.pojo.BackupInfo;
import com.cloud_guest.result.Result;
import com.cloud_guest.result.page.AbsPage;
import com.cloud_guest.result.page.ResultPage;
import com.cloud_guest.service.DataBackupRecoveryService;
import com.cloud_guest.utils.StrUtils;
import com.cloud_guest.utils.object.ObjectUtils;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/3/15 21:26:41
 * @Description
 */
@Slf4j
@Tag(name = "数据备份与恢复")
@RestController
@RequestMapping(value = {"/jwt/data/"})
public class DataBackupRecoveryController implements AbsPage {

    static {
        // 备份信息
        ClassConvert.register(BackupInfo.class, BackUp.class, info -> {
            String id = ObjectUtils.isEmpty(info.getId()) ? null : String.valueOf(info.getId());
            return new BackUp(id.toString(), info.getBackupName(), info.getBackupPath(), info.getBackupJson(), info.getBackupTime(), info.getBackupSize());
        }, info -> {
            String id = info.getId();
            return new BackupInfo(StrUtils.isNotBlank(id) ? Long.valueOf(id) : null, info.getBackupName(), info.getBackupPath(), info.getBackupJson(), info.getBackupTime(), info.getBackupSize());
        });
    }

    @Resource
    private DataBackupRecoveryService dataBackupRecoveryService;

    @SneakyThrows
    @SysLog
    @Operation(summary = "数据备份")
    @GetMapping("backup")
    public Result backup() {
        BackupInfo backup = dataBackupRecoveryService.backup();
        return Result.ok(backup);
    }

    @SneakyThrows
    @SysLog
    @Operation(summary = "查询远程数据备份分页")
    @GetMapping("backup/page")
    public Result backupPage(@RequestParam Long pageNumber, @RequestParam Long pageSize) {
        PageUtils.startPage(pageNumber, pageSize);
        List<BackupInfo> list = dataBackupRecoveryService.list();
        ResultPage<BackupInfo> data = listToPage(list);
        List<BackUp> backUpList = data.getList().stream()
                .map(info-> ClassConvert.convert(BackupInfo.class, BackUp.class, info))
                .toList();
        return Result.ok(pageToVoPage(data, backUpList));
    }
    @SneakyThrows
    @SysLog
    @Operation(summary = "查询本地数据备份列表")
    @GetMapping("backup/local")
    public Result backupLocal() {
        List<BackUp> list = dataBackupRecoveryService.localList().stream().map(info-> ClassConvert.convert(BackupInfo.class, BackUp.class, info)).toList();
        return Result.ok(list);
    }
    @SneakyThrows
    @SysLog
    @Operation(summary = "数据备份下载")
    @GetMapping("backup/download")
    public void backup(HttpServletResponse response, @RequestParam(required = false) String id) {
        Long idL = ObjectUtils.isEmpty(id) ? null : Long.parseLong(id);
        BackupInfo backup = ObjectUtils.isNotEmpty(idL) ? dataBackupRecoveryService.getById(idL) : dataBackupRecoveryService.backup();
        if (ObjectUtils.isEmpty(backup)){
            throw new GlobalException("备份不存在");
        }
        dataBackupRecoveryService.downLoadFileMultiThread(response, backup.getBackupName(), backup.getBackupJson().getBytes(StandardCharsets.UTF_8));
    }

    @SysLog
    @Operation(summary = "批量删除备份")
    @DeleteMapping("backup/batch")
    public Result<?> deleteBatchBackup(@RequestParam String ids) {
        if (ObjectUtils.isEmpty(ids)) {
            throw new GlobalException("请选择要删除的备份");
        }
        List<Long> idsList = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
        if (CollUtil.isEmpty(idsList)) {
            throw new GlobalException("请选择要删除的备份");
        }
        dataBackupRecoveryService.deleteBatchBackup(idsList);
        return Result.ok();
    }
    @SysLog
    @Operation(summary = "[本地]批量删除备份")
    @PostMapping("backup/batch/local")
    public Result<?> deleteBatchBackupLocal(@RequestBody List<String> paths) {
        if (CollUtil.isEmpty(paths)) {
            throw new GlobalException("请选择要删除的备份");
        }
        dataBackupRecoveryService.deleteBatchBackupLocal(paths);
        return Result.ok();
    }
    @SysLog
    @Operation(summary = "数据恢复")
    @GetMapping("recovery")
    public Result<?> recovery(@RequestParam(required = false) String id,
                              @RequestParam boolean isLocal,
                              @RequestParam(required = false) String name) {
        Long idL = ObjectUtils.isEmpty(id) ? null : Long.parseLong(id);
        dataBackupRecoveryService.recovery(isLocal, idL, name);
        return Result.ok();
    }
    @SysLog
    @Operation(summary = "数据恢复")
    @PostMapping("recovery/json")
    public Result<?> recoveryJson(@Validated @RequestBody JsonDto json) {
        Map<String, Object> map = JSONUtil.toBean(json.getJson(), Map.class);
        dataBackupRecoveryService.recovery(map);
        return Result.ok();
    }

    @SneakyThrows
    @SysLog
    @Operation(summary = "数据恢复")
    @PostMapping("recovery/file")
    public Result<?> recoveryFile(@RequestPart MultipartFile file) {
        // 读取文件内容
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        Map<String, Object> map = JSONUtil.toBean(content, Map.class);
        dataBackupRecoveryService.recovery(map);
        return Result.ok();
    }
}
