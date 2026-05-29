package com.cloud_guest.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.cloud_guest.entitys.domain.SystemInfo;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author yan
 * @Date 2026/3/17 19:55:31
 * @Description 系统信息工具类
 */
@Slf4j
public class SystemUtils {
    public static void main(String[] args) {
        printSystemInfo();
    }

    /**
     * 获取系统信息
     *
     * @return SystemInfoVO
     */
    public static SystemInfo initSystemInfo() {
        SystemInfo vo = new SystemInfo();

        Properties props = System.getProperties();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            vo.setHostName(localHost.getHostName())
                    .setIpAddress(localHost.getHostAddress());
        } catch (Exception e) {
            log.warn("无法获取主机信息：{}", e.getMessage());
            vo.setHostName("unknown").setIpAddress("unknown");
        }

        vo.setOsName(props.getProperty("os.name"))
                .setOsVersion(props.getProperty("os.version"))
                .setOsArch(props.getProperty("os.arch"))
                .setJvmName(runtimeMXBean.getVmName())
                .setJvmVersion(runtimeMXBean.getSpecVersion())
                //.setJvmStartTime(new java.util.Date(runtimeMXBean.getStartTime()))
                .setJvmStartTime(DateUtil.format(new java.util.Date(runtimeMXBean.getStartTime()), DatePattern.NORM_DATETIME_PATTERN))
                .setJvmUptimeSeconds((System.currentTimeMillis() - runtimeMXBean.getStartTime()) / 1000)
                .setJvmStartTimeStamp(runtimeMXBean.getStartTime())
                .setHeapInitMB(heapUsage.getInit() / 1024 / 1024)
                .setHeapMaxMB(heapUsage.getMax() / 1024 / 1024)
                .setHeapUsedMB(heapUsage.getUsed() / 1024 / 1024)
                .setCpuCores(Runtime.getRuntime().availableProcessors())
                .setUserDir(props.getProperty("user.dir"))
                .setUserHome(props.getProperty("user.home"))
                .setUserName(props.getProperty("user.name"));

        return vo;
    }
    public static SystemInfo updateSystemInfo(SystemInfo systemInfo) {
        systemInfo.setJvmUptimeSeconds((System.currentTimeMillis() - systemInfo.getJvmStartTimeStamp()) / 1000);
        return systemInfo;
    }

    /**
     * 打印系统信息到控制台
     */
    public static void printSystemInfo() {
        SystemInfo info = initSystemInfo();
        System.out.println("========== 系统信息 ==========");
        System.out.println("主机名：" + info.getHostName());
        System.out.println("IP 地址：" + info.getIpAddress());
        System.out.println("操作系统：" + info.getOsName() + " " + info.getOsVersion() + " (" + info.getOsArch() + ")");
        System.out.println("JVM: " + info.getJvmName() + " " + info.getJvmVersion());
        System.out.println("JVM 启动时间：" + info.getJvmStartTime());
        System.out.println("JVM 运行时长：" + info.getJvmUptimeSeconds() + "秒");
        System.out.println("堆内存：" + info.getHeapUsedMB() + "/" + info.getHeapMaxMB() + " MB");
        System.out.println("CPU 核心数：" + info.getCpuCores());
        System.out.println("用户目录：" + info.getUserDir());
        System.out.println("用户名：" + info.getUserName());
        System.out.println("============================");
    }

    /**
     * 获取 CPU 核心数
     */
    public static int getCpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * 获取 JVM 运行时长（秒）
     */
    public static long getJvmUptimeSeconds() {
        return (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000;
    }

    /**
     * 获取堆内存使用情况（MB）
     */
    public static Map<String, Long> getHeapMemoryUsageMB() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();

        Map<String, Long> usage = new HashMap<>();
        usage.put("init", heapUsage.getInit() / 1024 / 1024);
        usage.put("max", heapUsage.getMax() / 1024 / 1024);
        usage.put("used", heapUsage.getUsed() / 1024 / 1024);
        usage.put("committed", heapUsage.getCommitted() / 1024 / 1024);

        return usage;
    }
}
