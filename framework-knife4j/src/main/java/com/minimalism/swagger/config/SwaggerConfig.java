package com.minimalism.swagger.config;

import cn.hutool.extra.spring.SpringUtil;
import com.minimalism.swagger.abs.AbsSwagger;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import javax.annotation.Resource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Map;

/**
 * @author yan
 * @date 2023/9/30 4:24
 */
@Data
@NoArgsConstructor
@Configuration
@Slf4j
@ConditionalOnBean(SwaggerConfiguration.class)
public class SwaggerConfig implements AbsSwagger {
    @Lazy
    @Resource
    private SwaggerConfiguration config;

    @Override
    public String getAuthorization() {
        String result = config.getAuthorization();
        return result;
    }

    //    @Bean
    public OpenAPI getOpenApiDocumentation() {

        ExternalDocumentation externalDocs = new ExternalDocumentation()
                .description(defaultIfEmpty(config.getExternalDocDesc()))
                .description(defaultIfEmpty(config.getExternalDocUrl()));

        Contact contact = new Contact()
                .name(defaultIfEmpty(config.getContactName()))
                .url(defaultIfEmpty(config.getContactUrl()))
                .email(defaultIfEmpty(config.getContactEmail()));

        License license = new License()
                .name(defaultIfEmpty(config.getLicense()))
                .url(defaultIfEmpty(config.getLicenseUrl()));

        Info info = new Info()
                .title(defaultIfEmpty(config.getTitle()))
                .description(defaultIfEmpty(config.getDescription()))
                .version(defaultIfEmpty(config.getVersion()))
                .contact(contact)
                .termsOfService(defaultIfEmpty(config.getTermsOfService()))
                .license(license);


        /**
         * 全局参数
         */
        // 设置 spring security jwt accessToken 认证的请求头 Authorization: Bearer xxx.xxx.xxx
        String authorization = getAuthorization();
        /*System.err.println(authorization);*/
        Map<String, SecurityScheme> securitySchemas = buildSecuritySchemes();

        Components components = new Components()
                .securitySchemes(securitySchemas);

        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList(authorization);

        OpenAPI openApi = new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(securityRequirement)
                .externalDocs(externalDocs);

        securitySchemas.keySet().forEach(key -> openApi.addSecurityItem(new SecurityRequirement().addList(key)));
        return openApi;
    }


    /**
     * 配置api组
     * 注意请和yaml中配置的组名不要重复(俩者配置同时生效)
     *
     * @return
     */

    //@Lazy//懒加载
    public static GroupedOpenApi beanBuildApiGroupedOpenApi() {
        return SpringUtil.getBean(SwaggerConfig.class).buildApiGroupedOpenApi();
    }

    /**
     * 配置jwt组
     * 注意请和yaml中配置的组名不要重复(俩者配置同时生效)
     *
     * @return
     */

    //@Lazy//懒加载
    public static GroupedOpenApi beanBuildJwtGroupedOpenApi() {
        return SpringUtil.getBean(SwaggerConfig.class).buildJwtGroupedOpenApi();
    }

    /**
     * 配置other组
     * 注意请和yaml中配置的组名不要重复(俩者配置同时生效)
     *
     * @return
     */

    //@Lazy//懒加载
    public static GroupedOpenApi beanBuildOtherGroupedOpenApi() {
        return SpringUtil.getBean(SwaggerConfig.class).buildOtherGroupedOpenApi();
    }


    @Bean
    public OpenAPI openAPI() {
        return buildOpenAPI();
    }


    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return buildOpenApiCustomizer();
    }
}
