package com.cloud_guest.runner;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.io.File;

/**
 * @Author yan
 * @Date 2026/5/11 4:04:04
 * @Description
 */
@Slf4j
//@Component
public class CacheRunner   {
    //@PostConstruct
    //public void init() {
    //    createCacheDir();
    //}

    public static void createCacheDir() {
        Environment env = SpringUtil.getBean(Environment.class);
        String CACHE_DIR = env.getProperty("local.cache.dir", "cache");
        log.info("初始化缓存目录：{}", CACHE_DIR);
        File file = FileUtil.newFile(CACHE_DIR);
        if (!file.exists()){
            file.mkdirs();
            log.info("创建缓存目录：{}",CACHE_DIR);
        }
    }

}
