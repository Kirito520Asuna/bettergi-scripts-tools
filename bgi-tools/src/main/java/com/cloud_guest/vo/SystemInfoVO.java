package com.cloud_guest.vo;


import com.cloud_guest.utils.ApplicationUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @Author yan
 * @Date 2026/3/17
 * @Description 系统信息 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SystemInfoVO {
    @Schema(description = "应用ID")
    private String applicationId = ApplicationUtil.getApplicationId();
    @Schema(description = "主机名")
    private String hostName;

    @Schema(description = "IP 地址")
    private String ipAddress;

    @Schema(description = "操作系统名称")
    private String osName;

    @Schema(description = "操作系统版本")
    private String osVersion;

    @Schema(description = "操作系统架构")
    private String osArch;

    @Schema(description = "JVM 名称")
    private String jvmName;

    @Schema(description = "JVM 版本")
    private String jvmVersion;

    @Schema(description = "JVM 启动时间")
    private String jvmStartTime;

    @Schema(description = "JVM 运行时长 (秒)")
    private Long jvmUptimeSeconds;

    @Schema(description = "堆内存初始值 (MB)")
    private Long heapInitMB;

    @Schema(description = "堆内存最大值 (MB)")
    private Long heapMaxMB;

    @Schema(description = "堆内存已使用 (MB)")
    private Long heapUsedMB;

    @Schema(description = "CPU 核心数")
    private Integer cpuCores;

    @Schema(description = "用户目录")
    private String userDir;

    @Schema(description = "用户家目录")
    private String userHome;

    @Schema(description = "用户名")
    private String userName;
}
