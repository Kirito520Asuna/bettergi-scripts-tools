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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/23 20:45:43
 * @Description
 */
@Component
public class ApplicationUtil {
    public static String applicationId = null;
    public static Long workId = 0l;
    public static List<String> nodeApplicationIds = new ArrayList<>();
    private static final String application_key = "ALL:application";
    private static final String application_work_key = "ALL:WORK:application";
    @Resource
    private CacheService cacheService;

    @PostConstruct
    public void init() {
        //上线
        String id = System.currentTimeMillis() + "@" + IdUtil.fastUUID();
        applicationId = id;
        Cache<String> cache = cacheService.find(application_key);
        if (cache != null && cache.getData() != null) {
            List<String> ids = JSONUtil.toList(cache.getData(), String.class);
            nodeApplicationIds = ids.stream().filter(e -> !ObjectUtils.equals(e, id)).distinct().collect(Collectors.toList());
        }

        List<String> applicationIds = getAllApplicationIds();
        cacheService.save(application_key, JSONUtil.toJsonStr(applicationIds));


        //初始化workId
        String works = cacheService.find(application_work_key, String.class);
        LinkedHashSet<Long> workIds = new LinkedHashSet<>();
        if (StrUtil.isNotBlank(works)) {
            if (JSONUtil.isTypeJSONArray(works)){
                JSONUtil.toList(works, String.class).stream().map(Long::valueOf).forEach(workIds::add);
            }else {
                workIds.add(Long.valueOf(works));
            }
        }
        workId ++;
        if (workIds.size()>0) {
            workId += workIds.stream().mapToLong(Long::longValue).max().getAsLong();
        }
        workIds.add(workId);
        cacheService.save(application_work_key, JSONUtil.toJsonStr(workIds));
    }

    @PreDestroy
    public void destroy() {
        //下线
        Cache<String> cache = cacheService.find(application_key);
        if (cache != null && cache.getData() != null) {
            List<String> ids = JSONUtil.toList(cache.getData(), String.class);
            List<String> list = ids.stream().filter(e -> !ObjectUtils.equals(e, applicationId)).distinct().collect(Collectors.toList());
            cacheService.save(application_key, JSONUtil.toJsonStr(list));


            //下线workId
            String works = cacheService.find(application_work_key, String.class);
            LinkedHashSet<Long> workIds = new LinkedHashSet<>();
            if (StrUtil.isNotBlank(works)) {
                if (JSONUtil.isTypeJSONArray(works)){
                    JSONUtil.toList(works, String.class).stream().map(Long::valueOf).forEach(workIds::add);
                }else {
                    workIds.add(Long.valueOf(works));
                }
            }
            workIds.remove(workId);
            cacheService.save(application_work_key, JSONUtil.toJsonStr(workIds));
        }
    }

    public static String getApplicationId() {
        return applicationId;
    }
    public static Long getWorkId() {
        return workId;
    }

    public static List<String> getNodeApplicationIds() {
        return nodeApplicationIds;
    }

    public static List<String> getAllApplicationIds() {
        List<String> list = new ArrayList<>();
        list.add(applicationId);
        list.addAll(nodeApplicationIds);
        list = list.stream().filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        return list;
    }
}
