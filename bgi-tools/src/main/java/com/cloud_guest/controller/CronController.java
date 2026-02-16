package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.domain.dto.CronDto;
import com.cloud_guest.domain.dto.CronListDto;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.CronService;
import com.cloud_guest.view.BasicJsonView;
import com.cloud_guest.vo.CronVo;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

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
    @SysLog
    @Operation(summary = "[时区为东八区]解析cron表达式获取俩个时间戳中符合条件的首个时间戳 没有就返回null")
    // Swagger API文档注解，描述接口功能
    @PostMapping("next-timestamp")  // HTTP POST映射，指定请求路径为"next-timestamp"
    @JsonView(BasicJsonView.BaseView.class)
    public Result<Long> getNextCronTimestamp(@JsonView(BasicJsonView.NextCronTimestampView.class)
                                             @Validated({BasicJsonView.NextCronTimestampView.class})
                                             @RequestBody CronDto cronDto) {  // 方法签名，接收经过验证的请求体参数
        return ok(cronService.findNearestExecutionAfter(cronDto.getCronExpression(), cronDto.getStartTimestamp(), cronDto.getEndTimestamp()));
    }  // 当前方法返回null，实际实现中应该返回计算得到的时间戳


    @SneakyThrows  // 使用Lombok的@SneakyThrows注解，简化异常处理
    @SysLog
    @Operation(summary = "[时区为东八区]大批量解析")  // Swagger API文档注解，描述接口功能
    @PostMapping("next-timestamp/all")  // HTTP POST映射，指定请求路径为"next-timestamp"
    public Result<List<CronVo>> getNextCronTimestampAll(@JsonView(BasicJsonView.NextCronTimestampALLView.class)
                                                @Validated({BasicJsonView.NextCronTimestampALLView.class})
                                                @RequestBody CronListDto cronDto) {  // 方法签名，接收经过验证的请求体参数
        List<CronVo> cronList=  cronService.findNearestExecutionAfterAll(cronDto.getCronList());
        return ok(cronList);
    }  // 当前方法返回null，实际实现中应该返回计算得到的时间戳
}
