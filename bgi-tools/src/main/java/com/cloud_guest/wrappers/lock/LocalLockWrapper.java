package com.cloud_guest.wrappers.lock;


import com.cloud_guest.constants.KeyConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author yan
 * @Date 2026/2/28 21:23:37
 * @Description 本地锁实现（支持自动过期）
 */
@Slf4j
public class LocalLockWrapper extends AbstractLockWrapper {
    private final String lockKey;
    private ReentrantLock reentrantLock;
    private volatile boolean locked = false;
    private ScheduledFuture<?> scheduledFuture;

    // 懒加载的定时任务调度器
    private static volatile ScheduledExecutorService scheduler;

    private static ScheduledExecutorService getScheduler() {
        if (scheduler == null) {
            synchronized (LocalLockWrapper.class) {
                if (scheduler == null) {
                    scheduler = Executors.newScheduledThreadPool(
                            2,
                            r -> new Thread(r, "local-lock-scheduler")
                    );
                }
            }
        }
        return scheduler;
    }

    @Override
    public long getWaitTime() {
        return waitTime;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public LocalLockWrapper(String lockKey) {
        this(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME);
    }

    public LocalLockWrapper(String lockKey, long waitTime, long leaseTime) {
        this(lockKey, waitTime, leaseTime, DEFAULT_TIME_UNIT);
    }

    public LocalLockWrapper(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        super(waitTime, leaseTime, timeUnit);
        this.lockKey = KeyConstants.local_lock_key + lockKey;
        this.reentrantLock = LOCAL_LOCKS.computeIfAbsent(lockKey, k -> new ReentrantLock());
    }

    @Override
    public boolean lock() {
        try {
            locked = reentrantLock.tryLock(waitTime, timeUnit);
            if (locked) {
                log.debug("本地锁获取成功：{}", lockKey);
                // 如果设置了租约时间，启动自动释放任务
                if (leaseTime > 0) {
                    scheduleAutoUnlock();
                }
            } else {
                log.warn("本地锁获取失败：{}", lockKey);
            }
            return locked;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("本地锁获取被中断：{}", lockKey, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit timeUnit) {
        try {
            locked = reentrantLock.tryLock(waitTime, timeUnit);
            if (locked) {
                log.debug("本地锁尝试获取成功：{}", lockKey);
                // 如果设置了租约时间，启动自动释放任务
                if (leaseTime > 0) {
                    scheduleAutoUnlock();
                }
            }
            return locked;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("本地锁尝试获取被中断：{}", lockKey, e.getMessage());
            return false;
        }
    }

    /**
     * 安排自动解锁任务
     */
    private void scheduleAutoUnlock() {
        try {
            // 取消之前的定时任务（如果有）
            if (scheduledFuture != null && !scheduledFuture.isDone()) {
                scheduledFuture.cancel(false);
            }

            // 安排新的自动解锁任务
            scheduledFuture = getScheduler().schedule(() -> {
                try {
                    if (locked && reentrantLock.isHeldByCurrentThread()) {
                        log.warn("本地锁租约时间已到，自动释放：{}, leaseTime: {} {}",
                                lockKey, leaseTime, timeUnit);
                        reentrantLock.unlock();
                        locked = false;
                    }
                } catch (Exception e) {
                    log.error("自动释放本地锁失败：{}", lockKey, e.getMessage());
                }
            }, leaseTime, timeUnit);
        } catch (Exception e) {
            log.error("安排自动释放任务失败：{}", lockKey, e.getMessage());
        }
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return reentrantLock.isHeldByCurrentThread();
    }

    @Override
    public void unlock() {
        // 取消定时任务
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }

        if (locked && reentrantLock.isHeldByCurrentThread()) {
            reentrantLock.unlock();
            locked = false;
            log.debug("本地锁释放成功：{}", lockKey);
        }
    }

    @Override
    public boolean isLocked() {
        return locked && reentrantLock.isLocked();
    }
}
