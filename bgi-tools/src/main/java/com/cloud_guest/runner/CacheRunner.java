package com.cloud_guest.runner;

import cn.hutool.core.io.FileUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;

/**
 * @Author yan
 * @Date 2026/5/11 4:04:04
 * @Description
 */
@Slf4j
@Component
@AutoConfigureBefore(DataSource.class)
public class CacheRunner {
    @Value("${local.cache.dir:./cache}")
    String CACHE_DIR;
    @PostConstruct
    public void init() {
        File file = FileUtil.newFile(CACHE_DIR);
        if (!file.exists()){
            file.mkdirs();
            log.info("创建缓存目录：{}",CACHE_DIR);
        }
    }
}
