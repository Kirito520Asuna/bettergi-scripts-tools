package com.cloud_guest.controller;

import com.cloud_guest.domain.CronDto;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.CronService;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.cloud_guest.result.Result.ok;

/**
 * @Author yan
 * @Date 2026/1/12 17:38:34
 * @Description
 */
@Tag(name = "Cron")
@RestController
@RequestMapping({"/cron/", "/api/cron/", "/jwt/cron/"})
public class CronController {
    @Resource
    private CronService cronService;

    @SneakyThrows  // 使用Lombok的@SneakyThrows注解，简化异常处理

/**
 * 解析cron表达式获取俩个时间戳中符合条件的首个时间戳 没有就返回null
 * 这是一个HTTP POST端点，用于处理获取下一个符合cron表达式时间戳的请求
 *
 * @param cronDto 包含cron表达式和相关时间信息的DTO对象
 * @return 符合条件的首个时间戳，如果没有则返回null
 */
    @Operation(summary = "解析cron表达式获取俩个时间戳中符合条件的首个时间戳 没有就返回null")  // Swagger API文档注解，描述接口功能
    @PostMapping("next-timestamp")  // HTTP POST映射，指定请求路径为"next-timestamp"
    public Result getNextCronTimestamp(@Validated @RequestBody CronDto cronDto) {  // 方法签名，接收经过验证的请求体参数
        return ok(cronService.findNearestExecutionAfter(cronDto.getCronExpression(), cronDto.getStartTimestamp(), cronDto.getEndTimestamp()));
    }  // 当前方法返回null，实际实现中应该返回计算得到的时间戳

}
