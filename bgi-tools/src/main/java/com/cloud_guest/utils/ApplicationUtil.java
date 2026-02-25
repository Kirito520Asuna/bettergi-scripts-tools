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
    public static Long datacenterId = 0l;
    public static List<String> nodeApplicationIds = new ArrayList<>();
    private static final String application_key = "ALL:application";
    private static final String application_datacenter_key = "ALL:DATACENTER:application";
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
        String works = cacheService.find(application_datacenter_key, String.class);
        LinkedHashSet<Long> datacenterIds = new LinkedHashSet<>();
        if (StrUtil.isNotBlank(works)) {
            if (JSONUtil.isTypeJSONArray(works)){
                JSONUtil.toList(works, String.class).stream().map(Long::valueOf).forEach(datacenterIds::add);
            }else {
                datacenterIds.add(Long.valueOf(works));
            }
        }
        datacenterId++;
        if (datacenterIds.size()>0) {
            datacenterId += datacenterIds.stream().mapToLong(Long::longValue).max().getAsLong();
        }
        datacenterIds.add(datacenterId);
        cacheService.save(application_datacenter_key, JSONUtil.toJsonStr(datacenterIds));
    }

    @PreDestroy
    public void destroy() {
        //下线
        Cache<String> cache = cacheService.find(application_key);
        if (cache != null && cache.getData() != null) {
            List<String> ids = JSONUtil.toList(cache.getData(), String.class);
            List<String> list = ids.stream().filter(e -> !ObjectUtils.equals(e, applicationId)).distinct().collect(Collectors.toList());
            cacheService.save(application_key, JSONUtil.toJsonStr(list));


            //下线datacenterId
            String datacenters = cacheService.find(application_datacenter_key, String.class);
            LinkedHashSet<Long> datacenterIds = new LinkedHashSet<>();
            if (StrUtil.isNotBlank(datacenters)) {
                if (JSONUtil.isTypeJSONArray(datacenters)){
                    JSONUtil.toList(datacenters, String.class).stream().map(Long::valueOf).forEach(datacenterIds::add);
                }else {
                    datacenterIds.add(Long.valueOf(datacenters));
                }
            }
            datacenterIds.remove(datacenterId);
            cacheService.save(application_datacenter_key, JSONUtil.toJsonStr(datacenterIds));
        }
    }

    public static String getApplicationId() {
        return applicationId;
    }
    public static Long getDatacenterId() {
        return datacenterId;
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
