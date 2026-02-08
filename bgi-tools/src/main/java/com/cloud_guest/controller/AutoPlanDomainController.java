package com.cloud_guest.controller;

import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.domain.AnalysisJsonFileDto;
import com.cloud_guest.domain.AutoPlanDomainDto;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.AutoPlanDomainService;
import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cloud_guest.result.Result.ok;

/**
 * @Author yan
 * @Date 2026/2/8 15:49:23
 * @Description
 */
@Tag(name = "自动秘境计划服务")
@RestController
@RequestMapping(value = {"/auto/plan/domain/", "/api/auto/plan/domain/", "/jwt/auto/plan/domain/"})
public class AutoPlanDomainController {

    @Resource
    private AutoPlanDomainService autoPlanDomainService;

    @PostMapping("json/all")
    @SysLog
    @Operation(summary = "存储JSON")
    public Result<String> saveAll(@JsonView(value = BasicJsonView.AutoPlanDomainALLView.class)
                                  @Validated(value = BasicJsonView.AutoPlanDomainALLView.class)
                                  @RequestBody AutoPlanDomainDto dto) {
        autoPlanDomainService.saveAll(dto.getJson());
        return ok();
    }

    @PostMapping("json")
    @SysLog
    @Operation(summary = "存储JSON")
    public Result<String> save(@JsonView(value = BasicJsonView.AutoPlanDomainView.class)
                               @Validated(value = BasicJsonView.AutoPlanDomainView.class)
                               @RequestBody AutoPlanDomainDto dto) {
        autoPlanDomainService.save(dto.getUid(), dto.getJson());
        return ok(dto.getUid());
    }


    @SysLog(result = false)
    @Operation(summary = "查询JSON")
    @GetMapping("json")
    public Result<List<Map<String, Object>>> info(@RequestParam String uid) {
        List<Map<String, Object>> list = autoPlanDomainService.find(uid);
        return Result.ok(list);
    }

    @SysLog
    @Operation(summary = "批量删除JSON")
    @DeleteMapping("json")
    public Result<Boolean> infoDel(@Validated @NotBlank @RequestParam String uidStr) {
        List<String> ids = Arrays.stream(uidStr.split(",")).collect(Collectors.toList());
        return Result.ok(autoPlanDomainService.delList(ids));
    }
}
