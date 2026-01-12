package com.minimalism.swagger.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


/**
 * @Author yan
 * @Date 2024/10/20 下午11:34:00
 * @Description
 */
@Slf4j
@Configuration
@ConditionalOnExpression("${springdoc.open.default-group-configs.enable:false}")
public class GroupedOpenApiBeanList {
    @PostConstruct
    public void init(){
        log.debug("[init]-[GroupedOpenApiBeanList]::[{}]", StrUtil.subBefore(getClass().getName(),"$",false));
    }
    @Bean
    @ConditionalOnExpression("${springdoc.open.default-group-configs.api:true}")
    public GroupedOpenApi api(){
        GroupedOpenApi openApi = SwaggerConfig.beanBuildApiGroupedOpenApi();
        log.debug("[init]-[GroupedOpenApi]::[{}]", "Api");
        return openApi;
    }
    @Bean
    @ConditionalOnExpression("${springdoc.open.default-group-configs.jwt:true}")
    public GroupedOpenApi jwt(){
        GroupedOpenApi openApi = SwaggerConfig.beanBuildJwtGroupedOpenApi();
        log.debug("[init]-[GroupedOpenApi]::[{}]", "Jwt");
        return openApi;
    }
    @Bean
    @ConditionalOnExpression("${springdoc.open.default-group-configs.other:true}")
    public GroupedOpenApi other(){
        GroupedOpenApi openApi = SwaggerConfig.beanBuildOtherGroupedOpenApi();
        log.debug("[init]-[GroupedOpenApi]::[{}]", "Other");
        return openApi;
    }
}
