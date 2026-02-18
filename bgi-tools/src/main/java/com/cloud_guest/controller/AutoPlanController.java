package com.cloud_guest.controller;

import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Token;
import com.cloud_guest.domain.dto.AutoPlanDTO;
import com.cloud_guest.domain.dto.AutoPlanJsonDto;
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
@RequestMapping(value = {"/auto/plan/", "/api/auto/plan/", "/jwt/auto/plan/"})
public class AutoPlanController {

    @Resource
    private AutoPlanService autoPlanService;
    @SysLog(result = false)
    @Operation(summary = "查询全部国家JSON")
    @GetMapping("country/json/all")
    public Result<List<Map<String, Object>>> infoCountryAll() {
        List<Map<String, Object>> list = autoPlanService.findCountryAll();
        return ok(list);
    }
    @PostMapping("country/json/all")
    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权token]存储全部国家JSON")
    public Result<String> saveCountryAll(@JsonView(value = BasicJsonView.AutoPlanDomainALLView.class)
                                        @Validated(value = BasicJsonView.AutoPlanDomainALLView.class)
                                        @RequestBody AutoPlanJsonDto dto) {
        autoPlanService.saveCountryAll(dto.getJson());
        return ok();
    }

    @PostMapping("domain/json/all")
    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权token]存储基础全部JSON")
    public Result<String> saveDomainAll(@JsonView(value = BasicJsonView.AutoPlanDomainALLView.class)
                                        @Validated(value = BasicJsonView.AutoPlanDomainALLView.class)
                                        @RequestBody AutoPlanJsonDto dto) {
        autoPlanService.saveDomainAll(dto.getJson());
        return ok();
    }

    @SysLog(result = false)
    @Operation(summary = "查询基础全部JSON")
    @GetMapping("domain/json/all")
    public Result<List<Map<String, Object>>> infoDomainAll() {
        List<Map<String, Object>> list = autoPlanService.findDomainAll();
        return ok(list);
    }

    //@PostMapping("json")
    //@SysLog
    //@Token
    //@Operation(summary = "[需要登录/授权token]存储UID映射JSON")
    //public Result<String> save(@JsonView(value = BasicJsonView.AutoPlanView.class)
    //                           @Validated(value = BasicJsonView.AutoPlanView.class)
    //                           @RequestBody AutoPlanJsonDto dto) {
    //    autoPlanService.save(dto.getUid(), dto.getJson());
    //    return ok(dto.getUid());
    //}

    @PostMapping("info")
    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权token]存储UID体力计划")
    public Result<String> saveInfo(@Validated @RequestBody AutoPlanDTO dto) {
        dto.checkValid();
        autoPlanService.save(dto.getUid(), JSONUtil.toJsonStr(dto.getAutoPlanList()));
        return ok(dto.getUid());
    }

    @SysLog
    @Operation(summary = "查询UID映射JSON")
    @GetMapping("json")
    public Result<List<AutoPlanVo>> info(@RequestParam String uid) {
        List<AutoPlanVo> autoPlanVos = autoPlanService.find(uid);
        return ok(autoPlanVos);
    }

    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权token]批量删除UID映射JSON")
    @DeleteMapping("json")
    public Result<Boolean> infoDel(@Validated @NotBlank @RequestParam String uidStr) {
        List<String> ids = Arrays.stream(uidStr.split(",")).collect(Collectors.toList());
        return ok(autoPlanService.delList(ids));
    }
}
