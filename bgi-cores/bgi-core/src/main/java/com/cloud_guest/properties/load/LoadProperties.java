package com.cloud_guest.properties.load;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.enums.OSType;
import com.cloud_guest.utils.object.ObjectUtils;
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

        List<String> exList = new ArrayList<>();
        OSType currentOSType = OSType.getCurrentOSType();
        for (String yamlPath : yamlPaths) {
            OSType osType = OSType.detectByPathFormat(yamlPath);
            if(ObjectUtils.equals(OSType.UNKNOWN, osType)){
                log.debug("{}是相对路径", yamlPath);
            }else if(OSType.isUnixLike(osType)){
                if(!OSType.isUnixLike(null)){
                    //地址 linux ,系统非linux
                    log.debug("[{}]{}是{}绝对路径",currentOSType, yamlPath,"[非类unix系统]");
                    exList.add(yamlPath);
                    continue;
                }
            }else if (!OSType.isUnixLike(null)){
                if(OSType.isUnixLike(osType)){
                    //地址 非linux ,系统linux
                    log.debug("[{}]{}是{}绝对路径",currentOSType, yamlPath,"[非类unix系统]");
                    exList.add(yamlPath);
                    continue;
                }
            }
        }

        yamlPaths.removeAll(exList);
    }

}
