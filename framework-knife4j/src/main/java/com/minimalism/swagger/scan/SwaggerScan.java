package com.minimalism.swagger.scan;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yan
 * @date 2023/9/30 5:30
 */
@ComponentScan(basePackages = {"com.**.**.config"}
        , basePackageClasses = {RestController.class}
)
@Configuration
public class SwaggerScan {
}
