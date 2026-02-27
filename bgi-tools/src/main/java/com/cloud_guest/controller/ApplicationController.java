package com.cloud_guest.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Login;
import com.cloud_guest.domain.dto.ApplicationDto;
import com.cloud_guest.properties.BgiRedisProperties;
import com.cloud_guest.redis.config.RedisConfiguration;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.ApplicationContextHolder;
import com.cloud_guest.utils.ApplicationUtil;
import com.cloud_guest.utils.bean.MapUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return Result.ok(applicationIds);
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
