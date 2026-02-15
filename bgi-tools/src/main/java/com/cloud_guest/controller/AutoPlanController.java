package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Token;
import com.cloud_guest.domain.AutoPlanDomainDto;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.AutoPlanService;
import com.cloud_guest.view.BasicJsonView;
import com.cloud_guest.vo.AutoPlanVo;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
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
@Slf4j
@Tag(name = "自动体力计划服务")
@RestController
@RequestMapping(value = {"/auto/plan/domain/", "/api/auto/plan/domain/", "/jwt/auto/plan/domain/"})
public class AutoPlanController {

    @Resource
    private AutoPlanService autoPlanService;

    @PostMapping("json/all")
    @SysLog @Token
    @Operation(summary = "[需要登录/授权token]存储基础全部JSON")
    public Result<String> saveAll(@JsonView(value = BasicJsonView.AutoPlanDomainALLView.class)
                                  @Validated(value = BasicJsonView.AutoPlanDomainALLView.class)
                                  @RequestBody AutoPlanDomainDto dto) {
        autoPlanService.saveAll(dto.getJson());
        return ok();
    }
    @SysLog(result = false)
    @Operation(summary = "查询基础全部JSON")
    @GetMapping("json/all")
    public Result<List<Map<String, Object>>> infoAll() {
        List<Map<String, Object>> list = autoPlanService.findAll();
        return ok(list);
    }

    @PostMapping("json")
    @SysLog @Token
    @Operation(summary = "[需要登录/授权token]存储UID映射JSON")
    public Result<String> save(@JsonView(value = BasicJsonView.AutoPlanDomainView.class)
                               @Validated(value = BasicJsonView.AutoPlanDomainView.class)
                               @RequestBody AutoPlanDomainDto dto) {
        autoPlanService.save(dto.getUid(), dto.getJson());
        return ok(dto.getUid());
    }


    @SysLog
    @Operation(summary = "查询UID映射JSON")
    @GetMapping("json")
    public Result<List<AutoPlanVo>> info(@RequestParam String uid) {
        List<AutoPlanVo> autoPlanVos = autoPlanService.find(uid);
        return ok(autoPlanVos);
    }

    @SysLog @Token
    @Operation(summary = "[需要登录/授权token]批量删除UID映射JSON")
    @DeleteMapping("json")
    public Result<Boolean> infoDel(@Validated @NotBlank @RequestParam String uidStr) {
        List<String> ids = Arrays.stream(uidStr.split(",")).collect(Collectors.toList());
        return ok(autoPlanService.delList(ids));
    }
}
