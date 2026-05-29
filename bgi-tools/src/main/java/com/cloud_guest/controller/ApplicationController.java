package com.cloud_guest.controller;

import cn.hutool.core.collection.CollUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Login;
import com.cloud_guest.entitys.dto.ApplicationDto;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.ApplicationContextHolder;
import com.cloud_guest.utils.ApplicationUtil;
import com.cloud_guest.entitys.domain.SystemInfo;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Author yan
 * @Date 2026/2/12 21:25:06
 * @Description
 */
@RestController
@RequestMapping(value = {"/api/application/", "/jwt/application/"})
public class ApplicationController {

    //@Resource
    //private RestartEndpoint restartEndpoint;


    @Login
    @SysLog
    @Operation(summary = "[需要登录]重启")
    @PostMapping("restart")
    public Result restart(@Validated @RequestBody ApplicationDto dto) {
        List<String> ids = dto.getIds();
        String applicationId = ApplicationUtil.getApplicationId();
        if (ids.contains(applicationId)) {
            //restartEndpoint.restart();
            ApplicationContextHolder.restart();
        } else {
            applicationId = null;
        }
        return Result.ok(applicationId);
    }

    @SysLog
    @Operation(summary = "获取所有分布ID")
    @GetMapping("applicationIds")
    public Result applicationIds() {
        List<String> applicationIds = ApplicationUtil.getAllApplicationIds();
        if (CollUtil.isEmpty(applicationIds)) {
            String currentId = ApplicationUtil.getApplicationId();
            if (StrUtil.isNotBlank(currentId)) {
                applicationIds = Collections.singletonList(currentId);
            }
        }
        return Result.ok(applicationIds);
    }

    @Login
    @SysLog
    @Operation(summary = "获取系统信息")
    @GetMapping("sys/info")
    public Result<SystemInfo> sysInfo(@RequestParam String ids) {
        List<String> applicationIds = new ArrayList<>();
        if (StrUtil.isNotBlank(ids)) {
            Arrays.stream(ids.split(",")).forEach(applicationIds::add);
        }
        String applicationId = ApplicationUtil.getApplicationId();
        SystemInfo systemInfo = null;
        if (applicationIds.contains(applicationId)) {
            systemInfo = ApplicationUtil.getNewSystemInfo();
        }
        return Result.ok(systemInfo);
    }
/*    @Login
    @SysLog
    @Operation(summary = "[需要登录]判断重启")
    @PostMapping("restart/info")
    public Result restartInfo(@Validated @RequestBody ApplicationDto dto) {
        List<String> ids = dto.getIds();
        String applicationId = ApplicationUtil.getApplicationId();
        if (!ids.contains(applicationId)) {
            applicationId= null;
        }
        return Result.ok(applicationId);
    }*/

/*    @Login
    @SysLog
    @Operation(summary = "获取redis配置信息")
    @GetMapping("redis/info")
    public Result redisInfo() {
        BgiRedisProperties bean = SpringUtil.getBean(BgiRedisProperties.class);
        Map<String, Object> redisMap = BeanUtil.beanToMap(bean);
        return Result.ok(redisMap);
    }

    @Login
    @SysLog
    @Operation(summary = "修改redis配置信息[重启生效]")
    @PostMapping("redis/info")
    public Result updateRedisInfo() {
        class RedisConfigInfo{
            private List<String> applicationIds=new ArrayList<>();
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            class RedisInfo{
                private RedisConfiguration.RedisMode mode = RedisConfiguration.RedisMode.none;
                private String url;
                private String host = "localhost";
                private int port = 6379;
                private int database = 0;

                private RedisProperties.Sentinel sentinel;
                private RedisProperties.Cluster cluster;

                private String username;
                private String password;
            }
        }
        BgiRedisProperties bgiRedisProperties = new BgiRedisProperties();
        Map<String, Object> map = MapUtils.createHierarchicalMap("spring.redis", bgiRedisProperties);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(map);
        return Result.ok();
    }*/
}
