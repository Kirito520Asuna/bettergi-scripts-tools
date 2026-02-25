package com.cloud_guest.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * @Author yan
 * @Date 2026/2/25 21:31:32
 * @Description
 */
public class IdUtils extends IdUtil {
    public static long datacenterId = ApplicationUtil.getDatacenterId();
    public static long workerId = getWorkerId(datacenterId, Long.MAX_VALUE);
    public static Snowflake createSnowflake() {
        return new Snowflake(workerId, datacenterId);
    }
    public static long getNextId() {
        return createSnowflake().nextId();
    }
    public static String getNextIdStr() {
        return createSnowflake().nextIdStr();
    }
}
