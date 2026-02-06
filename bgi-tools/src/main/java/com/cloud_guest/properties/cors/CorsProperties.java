package com.cloud_guest.properties.cors;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.properties.PropertiesConstants;
import com.cloud_guest.properties.bean.BeanProperties;
import com.cloud_guest.utils.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * @Author minimalism
 * @Date 2024/11/12 下午7:47:04
 * @Description
 */
@Accessors(chain = true)
@Configuration
@NoArgsConstructor
@Data
@AllArgsConstructor
@ConditionalOnMissingBean(CorsProperties.class)
@ConditionalOnExpression("${" + PropertiesConstants.CORS_ENABLED + ":true}")
@ConfigurationProperties(prefix = PropertiesConstants.CORS_PREFIX)
public class CorsProperties implements BeanProperties {
    private String pattern;
    private String allowedOrigin;
    private String allowedOriginPattern;
    private String allowedMethods;
    private String allowedHeader;
    private Boolean allowCredentials;
    private Long maxAge;
    private CorsGatewayProperties gatewayCorsProperties;

    public String[] AllowedMethods() {
        String[] origins;
        if (StrUtil.isNotBlank(allowedMethods)) {
            origins = allowedMethods.split(",");
        } else {
            CorsGatewayProperties bean = gatewayCorsProperties;
            if (bean == null) {
                try {
                    bean = SpringUtil.getBean(CorsGatewayProperties.class);
                } catch (Exception e) {
                    log().warn(e.getMessage());
                    bean = new CorsGatewayProperties();
                }
                gatewayCorsProperties = bean;
            }
            Boolean defaultFilter = ObjectUtils.defaultIfEmpty(bean.getDefaultFilter(), true);
            Boolean webFilter = ObjectUtils.defaultIfEmpty(bean.getWebFilter(), false);
            boolean originsBool = defaultFilter || webFilter;
            origins = originsBool ? CorsConstants.GATEWAY_ORIGINS : CorsConstants.ORIGINS;
        }
        return origins;
    }
}
