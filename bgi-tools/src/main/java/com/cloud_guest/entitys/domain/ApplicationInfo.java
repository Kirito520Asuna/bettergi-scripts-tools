package com.cloud_guest.entitys.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/3/2 22:42:44
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationInfo {
    public String applicationId;
    public Long datacenterId;
    //上报时间
    public Long timeStamp;
    //用于日志分析
    public Long timeout;
    public String cronExpression;

    public ApplicationInfo(String applicationId, Long datacenterId, Long timeStamp) {
        this.applicationId = applicationId;
        this.datacenterId = datacenterId;
        this.timeStamp = timeStamp;
    }

    public ApplicationInfo toReportedOnline() {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setApplicationId(this.getApplicationId());
        applicationInfo.setDatacenterId(this.getDatacenterId());
        applicationInfo.setTimeout(this.getTimeout());
        applicationInfo.setTimeStamp(System.currentTimeMillis());
        return applicationInfo;
    }
}
