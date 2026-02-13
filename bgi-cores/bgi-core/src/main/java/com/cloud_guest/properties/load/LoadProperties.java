package com.cloud_guest.properties.load;

import cn.hutool.extra.spring.SpringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/12 22:12:33
 * @Description
 */
@Component
@Data
@Slf4j
public class LoadProperties {

    private List<String> yamlPaths = new ArrayList<>();

    @PostConstruct
    private void init() {
        Environment env = SpringUtil.getBean(Environment.class);
        boolean readNull = true;
        int index = 0;
        String key = "spring.config.import";
        while (true) {
            String value = env.getProperty(key + "[" + index + "]");
            readNull = value == null;
            if (readNull) {
                break;
            } else {
                yamlPaths.add(value.replace("optional:file:", ""));
            }
            index++;
        }
    }

}
