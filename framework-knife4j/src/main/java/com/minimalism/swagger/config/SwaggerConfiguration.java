package com.minimalism.swagger.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author yan
 * @Date 2024/5/27 0027 13:19
 * @Description
 */
@Configuration
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class SwaggerConfiguration {
    @Value(value = "${api.authorization:Authorization}")
    private String authorization;
    @Value(value = "${api.common.version:1.0.0}")
    private String version;
    @Value(value = "${api.common.title:标题}")
    private String title;
    @Value(value = "${api.common.description:说明}")
    private String description;
    @Value(value = "${api.common.termsOfService:}")
    private String termsOfService;
    @Value(value = "${api.common.license:}")
    private String license;
    @Value(value = "${api.common.licenseUrl:}")
    private String licenseUrl;
    @Value(value = "${api.common.externalDocDesc:}")
    private String externalDocDesc;
    @Value(value = "${api.common.externalDocUrl:}")
    private String externalDocUrl;
    @Value(value = "${api.common.contact.name:}")
    private String contactName;
    @Value(value = "${api.common.contact.url:}")
    private String contactUrl;
    @Value(value = "${api.common.contact.email:}")
    private String contactEmail;
    @Value(value = "${server.servlet.context-path:}")
    private String serverServletContextPath;
}
