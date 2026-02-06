package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    @PostMapping("json/bytes")
    @SysLog
    @Operation(summary = "")
    public Result<String> saveBytes() {
        return ok();
    }
    @SneakyThrows
    @PostMapping("json/file")
    @SysLog
    @Operation(summary = "")
    public Result<String> saveFileToJson(@RequestPart MultipartFile file) {
        return ok();
    }
}
