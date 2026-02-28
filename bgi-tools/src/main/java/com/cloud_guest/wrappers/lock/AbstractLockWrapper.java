package com.cloud_guest.wrappers.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author yan
 * @Date 2026/2/28 21:26:02
 * @Description
 */
public abstract class AbstractLockWrapper implements LockWrapper  {

    // 本地锁容器
    protected static final ConcurrentHashMap<String, ReentrantLock> LOCAL_LOCKS = new ConcurrentHashMap<>();

    // 锁的默认超时时间（毫秒）
    protected static final long DEFAULT_TIMEOUT = 10000L;

    // 锁的默认等待时间（毫秒）
    protected static final long DEFAULT_WAIT_TIME = 5000L;
    // 锁的默认时间单位
    protected static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    @Override
    public long getWaitTime() {
        return DEFAULT_WAIT_TIME;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return DEFAULT_TIME_UNIT;
    }
}
