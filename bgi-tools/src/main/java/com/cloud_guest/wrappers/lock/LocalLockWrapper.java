package com.cloud_guest.wrappers.lock;

/**
 * @Author yan
 * @Date 2026/2/28 21:23:37
 * @Description
 */

import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.utils.LockUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 本地锁实现
 */
@Slf4j
public class LocalLockWrapper extends AbstractLockWrapper {
    private final String lockKey;
    private final long timeout;
    private final TimeUnit timeUnit;
    private ReentrantLock reentrantLock;
    private volatile boolean locked = false;
    @Override
    public long getWaitTime() {
        return timeout;
    }
    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    public LocalLockWrapper(String lockKey) {
        this(lockKey, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
    }
    public LocalLockWrapper(String lockKey, long timeout) {
        this(lockKey, timeout, DEFAULT_TIME_UNIT);
    }
    public LocalLockWrapper(String lockKey, long timeout, TimeUnit timeUnit) {
        this.lockKey = KeyConstants.local_lock_key + lockKey;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.reentrantLock = LOCAL_LOCKS.computeIfAbsent(lockKey, k -> new ReentrantLock());
    }

    @Override
    public boolean lock() {
        try {
            locked = reentrantLock.tryLock(timeout, timeUnit);
            if (locked) {
                log.debug("本地锁获取成功: {}", lockKey);
            } else {
                log.warn("本地锁获取失败: {}", lockKey);
            }
            return locked;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("本地锁获取被中断: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public boolean tryLock() {
        return tryLock(timeout, timeUnit);
    }

    @Override
    public boolean tryLock(long waitTime, TimeUnit timeUnit) {
        try {
            locked = reentrantLock.tryLock(waitTime, timeUnit);
            if (locked) {
                log.debug("本地锁尝试获取成功: {}", lockKey);
            }
            return locked;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("本地锁尝试获取被中断: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public void unlock() {
        if (locked && reentrantLock.isHeldByCurrentThread()) {
            reentrantLock.unlock();
            locked = false;
            log.debug("本地锁释放成功: {}", lockKey);
        }
    }

    @Override
    public boolean isLocked() {
        return locked && reentrantLock.isLocked();
    }
}