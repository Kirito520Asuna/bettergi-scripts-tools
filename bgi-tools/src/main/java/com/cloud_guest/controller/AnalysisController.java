package com.cloud_guest.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.domain.AnalysisJsonFileDto;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.FileJsonService;
import com.cloud_guest.utils.LocalCacheUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.cloud_guest.result.Result.ok;

/**
 * @Author yan
 * @Date 2026/2/6 16:18:45
 * @Description
 */
@Tag(name = "解析服务")
@RestController
@RequestMapping(value = {"/analysis/", "/api/analysis/", "/jwt/analysis/"})
public class AnalysisController {
    @Resource
    private FileJsonService fileJsonService;

    @PostMapping("json/bytes")
    @SysLog
    @Operation(summary = "加载JSON文件Bytes准备解析(返回唯一id)")
    public Result<String> saveBytes(@Validated @RequestBody AnalysisJsonFileDto dto) {
        String id = fileJsonService.save(dto.getFilename(), dto.getBytes());
        return ok(id);
    }

    @SneakyThrows
    @PostMapping("json/file")
    @SysLog
    @Operation(summary = "加载JSON文件准备解析(返回唯一id)")
    public Result<String> saveFileToJson(@RequestPart MultipartFile file) {
        byte[] bytes = file.getBytes();
        String filename = file.getOriginalFilename();
        String id = fileJsonService.save(filename, bytes);
        return ok(id);
    }

    @SysLog
    @Operation(summary = "查询JSON")
    @GetMapping("json/file")
    public Result<Cache<String>> info(@RequestParam String id) {
        return Result.ok(fileJsonService.find(id));
    }

    @SysLog
    @Operation(summary = "批量删除JSON")
    @DeleteMapping("json/file")
    public Result<Boolean> infoDel(@Validated @NotBlank @RequestParam String idStr) {
        List<String> ids = Arrays.stream(idStr.split(",")).collect(Collectors.toList());
        return Result.ok(fileJsonService.del(ids));
    }

}
