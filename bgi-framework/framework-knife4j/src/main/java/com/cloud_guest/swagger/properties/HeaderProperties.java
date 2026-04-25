package com.cloud_guest.swagger.properties;

import com.cloud_guest.swagger.properties.domain.ApiHeader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/4/1 1:26:48
 * @Description
 */
@Component
@ConfigurationProperties(prefix = "springdoc.open.default-group-configs.header")
@Data
public class HeaderProperties {
    List<ApiHeader> api=new ArrayList<>();
}
