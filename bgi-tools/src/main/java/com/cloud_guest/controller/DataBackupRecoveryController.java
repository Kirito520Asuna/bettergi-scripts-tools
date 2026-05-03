package com.cloud_guest.controller;

import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.domain.dto.JsonDto;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.pojo.BackupInfo;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.DataBackupRecoveryService;
import com.cloud_guest.utils.object.ObjectUtils;
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

/**
 * @Author yan
 * @Date 2026/3/15 21:26:41
 * @Description
 */
@Slf4j
@Tag(name = "数据备份与恢复")
@RestController
@RequestMapping(value = {"/jwt/data/"})
public class DataBackupRecoveryController {
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
    @Operation(summary = "数据备份下载")
    @GetMapping("backup/download")
    public void backup(HttpServletResponse response, @RequestParam(required = false) Long id) {
        BackupInfo backup = ObjectUtils.isNotEmpty(id) ? dataBackupRecoveryService.getById(id) : dataBackupRecoveryService.backup();
        if (ObjectUtils.isEmpty(backup)){
            throw new GlobalException("备份不存在");
        }
        dataBackupRecoveryService.downLoadFileMultiThread(response, backup.getBackupName(), backup.getBackupJson().getBytes(StandardCharsets.UTF_8));
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
    public Result<?> recovery(@RequestPart MultipartFile file) {
        // 读取文件内容
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        Map<String, Object> map = JSONUtil.toBean(content, Map.class);
        dataBackupRecoveryService.recovery(map);
        return Result.ok();
    }
}
