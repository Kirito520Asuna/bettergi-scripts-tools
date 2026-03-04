package com.cloud_guest.wrappers.lock;
import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2026/2/28 21:22:45
 * @Description 锁包装器接口
 */
public interface LockWrapper {
    /**
     * 获取锁
     *
     * @return boolean 是否获取成功
     */
    boolean lock();

    /**
     * 尝试获取锁
     *
     * @return boolean 是否获取成功
     */
    boolean tryLock();

    /**
     * 尝试获取锁 - 指定等待时间
     *
     * @param waitTime 等待时间
     * @param timeUnit 时间单位
     * @return boolean 是否获取成功
     */
    boolean tryLock(long waitTime, TimeUnit timeUnit);

    /**
     * 尝试获取锁 - 指定等待时间和租约时间
     * @param waitTime
     * @param leaseTime
     * @param timeUnit
     * @return
     */
    boolean tryLock(long waitTime,long leaseTime, TimeUnit timeUnit);

    boolean isHeldByCurrentThread();
    /**
     * 释放锁
     */
    void unlock();

    /**
     * 判断是否持有锁
     *
     * @return boolean
     */
    boolean isLocked();

    /**
     * 获取等待时间
     * @return
     */
    long getWaitTime();

    /**
     * 获取时间单位
     * @return
     */
    TimeUnit getTimeUnit();
}