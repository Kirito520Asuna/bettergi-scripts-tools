package com.cloud_guest.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.service.FileJsonService;
import com.cloud_guest.utils.LocalCacheUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/6 18:10:53
 * @Description
 */
@Service
public class FileJsonServiceImpl implements FileJsonService {
    @Override
    public String save(String filename, byte[] bytes) {
        String path = FileUtil.getTmpDir() + "/json/file/" + IdUtil.fastUUID() + System.currentTimeMillis() + filename;
        try {
            String id = IdUtil.fastUUID() + System.currentTimeMillis();
            // 直接写入文件（自动创建目录、覆盖写入）
            FileUtil.writeBytes(bytes, path);
            String json = FileUtil.readUtf8String(path);
            Cache<String> cache = new Cache<>();
            cache.setType("json");
            cache.setData(json);
            LocalCacheUtils.put(id, cache);
            return id;
        } finally {
            FileUtil.del(path);
        }
    }

    @Override
    public Cache<String> find(String id) {
        Cache<String> cache = (Cache<String>) LocalCacheUtils.get(id);
        return cache;
    }

    @Override
    public boolean del(List<String> ids) {
        LocalCacheUtils.removeList(ids);
        return true;
    }
}
