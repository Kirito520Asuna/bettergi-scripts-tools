package com.cloud_guest.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.cloud_guest.aop.bean.AbsBean;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.ApplicationInfo;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.object.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
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
    private static ApplicationInfo applicationInfo = new ApplicationInfo(null, 0l, System.currentTimeMillis());
    private static boolean initEnd = false;
    @PostConstruct
    @Override
    public void init() {
        AbsBean.super.init();
        log.debug("==> 初始化ApplicationUtil <==");
        initApplicationInfo();
        initEnd = true;
    }
    @PreDestroy
    @Override
    public void destroy() {
        AbsBean.super.destroy();
        log.debug("==> 销毁ApplicationUtil <==");
        //下线
        destroyApplicationInfo();
    }

    public static void initApplicationInfo() {
        List<ApplicationInfo> applicationInfos = ApplicationContextHolder.checkAndGetOnline(null);
        //上线
        String id = System.currentTimeMillis() + "@" + IdUtil.fastUUID();
        applicationInfo.setApplicationId(id);
        Long datacenterId = applicationInfo.getDatacenterId();
        datacenterId++;
        datacenterId += CollUtil.isEmpty(applicationInfos) ? 0l : applicationInfos.stream().map(ApplicationInfo::getDatacenterId).filter(ObjectUtils::isNotEmpty).mapToLong(Long::longValue).max().getAsLong();
        applicationInfo.setDatacenterId(datacenterId);
        ApplicationContextHolder.reportedOnline(applicationInfo.toReportedOnline());
    }

    /**
     * 销毁应用ID和数据中心的公共方法
     * 该方法调用重载的destroyApplicationIdAndDatacenterId方法，使用当前实例的applicationId和datacenterId作为参数
     */
    public static void destroyApplicationInfo() {
        ApplicationContextHolder.clearReportedOnline(applicationInfo);
    }
    public static void setCronExpression(String cronExpression) {
        applicationInfo.setCronExpression(cronExpression);
    }
    public static void setTimeout(Long timeout) {
        applicationInfo.setTimeout(timeout);
    }

    public static ApplicationInfo getApplicationInfo() {
        if (!initEnd) {
            return null;
        }
        return applicationInfo;
    }

    public static String getApplicationId() {
        return applicationInfo.getApplicationId();
    }

    public static Long getDatacenterId() {
        return applicationInfo.getDatacenterId();
    }

    public static List<ApplicationInfo> getAllOnlineApplicationInfos() {
        return ApplicationContextHolder.checkAndGetOnline(null);
    }

    public static List<String> getAllApplicationIds() {
        return getAllOnlineApplicationInfos().stream().map(ApplicationInfo::getApplicationId).collect(Collectors.toList());
    }
}
