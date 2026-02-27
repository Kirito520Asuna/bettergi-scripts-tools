package com.cloud_guest.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.bean.AbsBean;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.object.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class ApplicationUtil implements AbsBean {
    public static String applicationId = null;
    public static Long datacenterId = 0l;
    //public static List<String> nodeApplicationIds = new ArrayList<>();
    private static final String application_key = "ALL:application";
    private static final String application_datacenter_key = "ALL:DATACENTER:application";
    @Resource
    private CacheService cacheService;

    @PostConstruct
    @Override
    public void init() {
        AbsBean.super.init();
        log.debug("==> 初始化ApplicationUtil <==");
        //上线
        String id = System.currentTimeMillis() + "@" + IdUtil.fastUUID();
        applicationId = id;
        //Cache<String> cache = cacheService.find(application_key);
        //if (cache != null && cache.getData() != null) {
        //    List<String> ids = JSONUtil.toList(cache.getData(), String.class);
        //    //nodeApplicationIds = ids.stream().filter(e -> !ObjectUtils.equals(e, id)).distinct().collect(Collectors.toList());
        //}

        List<String> applicationIds = getAllApplicationIds();
        applicationIds.add(id);
        cacheService.save(application_key, JSONUtil.toJsonStr(applicationIds));


        //初始化workId
        String works = cacheService.findById(application_datacenter_key);
        LinkedHashSet<Long> datacenterIds = new LinkedHashSet<>();
        if (StrUtil.isNotBlank(works)) {
            if (JSONUtil.isTypeJSONArray(works)) {
                JSONUtil.toList(works, String.class).stream().map(Long::valueOf).forEach(datacenterIds::add);
            } else {
                datacenterIds.add(Long.valueOf(works));
            }
        }
        datacenterId++;
        if (datacenterIds.size() > 0) {
            datacenterId += datacenterIds.stream().filter(ObjectUtils::isNotEmpty).mapToLong(Long::longValue).max().getAsLong();
        }
        datacenterIds.add(datacenterId);
        cacheService.save(application_datacenter_key, JSONUtil.toJsonStr(datacenterIds));
    }

    @PreDestroy
    @Override
    public void destroy() {
        AbsBean.super.destroy();
        log.debug("==> 销毁ApplicationUtil <==");
        //下线
        Cache<String> cache = cacheService.find(application_key);
        List<String> list = new ArrayList<>();
        if (cache != null && cache.getData() != null) {
            List<String> ids = JSONUtil.toList(cache.getData(), String.class);
            list = ids.stream().filter(e -> !ObjectUtils.equals(e, applicationId)).distinct().collect(Collectors.toList());
        }
        cacheService.save(application_key, JSONUtil.toJsonStr(list));

        //下线datacenterId
        String datacenters = cacheService.findById(application_datacenter_key);
        LinkedHashSet<Long> datacenterIds = new LinkedHashSet<>();
        if (StrUtil.isNotBlank(datacenters)) {
            if (JSONUtil.isTypeJSONArray(datacenters)) {
                JSONUtil.toList(datacenters, String.class).stream().filter(ObjectUtils::isNotEmpty).map(Long::valueOf).forEach(datacenterIds::add);
            } else {
                datacenterIds.add(Long.valueOf(datacenters));
            }
        }
        datacenterIds.remove(datacenterId);
        cacheService.save(application_datacenter_key, JSONUtil.toJsonStr(datacenterIds));
    }

    public static String getApplicationId() {
        return applicationId;
    }

    public static Long getDatacenterId() {
        return datacenterId;
    }

    public static List<String> getAllApplicationIds() {
        List<String> list = new ArrayList<>();
        Cache<String> cache = SpringUtil.getBean(CacheService.class).find(application_key);
        if (cache != null && cache.getData() != null) {
            List<String> ids = JSONUtil.toList(cache.getData(), String.class);
            list.addAll(ids);
        }
        //List<String> list = new ArrayList<>();
        //list.add(applicationId);
        //list.addAll(nodeApplicationIds);
        list = list.stream().filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        return list;
    }
}
