package com.cloud_guest.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.object.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/23 20:45:43
 * @Description
 */
@Component
public class ApplicationUtil {
    public static String ApplicationId = null;
    public static List<String> nodeApplicationIds = new ArrayList<>();
    private static final String application_key = "ALL:application";
    private static final String application_map_key = "ALL:MAP:application";
    @Resource
    private CacheService cacheService;

    @PostConstruct
    public void init() {
        //上线
        String id = System.currentTimeMillis() + "@" + IdUtil.fastUUID();
        ApplicationId = id;
        Cache<String> cache = cacheService.find(application_key);
        if (cache != null && cache.getData() != null) {
            List<String> ids = JSONUtil.toList(cache.getData(), String.class);
            nodeApplicationIds = ids.stream().filter(e -> !ObjectUtils.equals(e, id)).distinct().collect(Collectors.toList());
        }

        List<String> applicationIds = getAllApplicationIds();
        cacheService.save(application_key, JSONUtil.toJsonStr(applicationIds));
    }

    @PreDestroy
    public void destroy() {
        //下线
        Cache<String> cache = cacheService.find(application_key);
        if (cache != null && cache.getData() != null) {
            List<String> ids = JSONUtil.toList(cache.getData(), String.class);
            List<String> list = ids.stream().filter(e -> !ObjectUtils.equals(e, ApplicationId)).distinct().collect(Collectors.toList());
            cacheService.save(application_key, JSONUtil.toJsonStr(list));
        }
    }

    public static String getApplicationId() {
        return ApplicationId;
    }

    public static List<String> getNodeApplicationIds() {
        return nodeApplicationIds;
    }

    public static List<String> getAllApplicationIds() {
        List<String> list = new ArrayList<>();
        list.add(ApplicationId);
        list.addAll(nodeApplicationIds);
        list = list.stream().filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        return list;
    }
}
