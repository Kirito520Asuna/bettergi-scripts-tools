package com.cloud_guest.wrappers.lock;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.constants.KeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2026/2/28 21:28:57
 * @Description
 */
@Slf4j
public class RedisLockWrapper extends AbstractLockWrapper {
    private final String lockKey;
    private final long timeout;
    private final TimeUnit timeUnit;
    private RLock rLock;
    private volatile boolean locked = false;

    @Override
    public long getWaitTime() {
        return timeout;
    }
    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    public RedisLockWrapper(String lockKey) {
        this(lockKey, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    public RedisLockWrapper(String lockKey, long timeout) {
        this(lockKey, timeout, DEFAULT_TIME_UNIT);
    }
    public RedisLockWrapper(String lockKey, long timeout, TimeUnit timeUnit) {
        this.lockKey = KeyConstants.redis_lock_key + lockKey;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        try {
            RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
            this.rLock = redissonClient.getLock(this.lockKey);
        } catch (Exception e) {
            log.error("获取RedissonClient失败，使用本地锁替代: {}", lockKey, e);
            // 降级到本地锁
            this.rLock = null;
        }
    }

    @Override
    public boolean lock() {
        if (rLock == null) {
            log.warn("Redis锁不可用，降级到本地锁: {}", lockKey);
            return new LocalLockWrapper(lockKey.replace(KeyConstants.redis_lock_key, KeyConstants.local_lock_key), timeout).lock();
        }

        try {
            locked = rLock.tryLock(timeout, timeUnit);
            if (locked) {
                log.debug("Redis分布式锁获取成功: {}", lockKey);
            } else {
                log.warn("Redis分布式锁获取失败: {}", lockKey);
            }
            return locked;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Redis分布式锁获取被中断: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public boolean tryLock() {
        if (rLock == null) {
            return new LocalLockWrapper(lockKey.replace(KeyConstants.redis_lock_key, KeyConstants.local_lock_key)).tryLock();
        }
        return tryLock(timeout, timeUnit);
    }

    @Override
    public boolean tryLock(long waitTime, TimeUnit timeUnit) {
        if (rLock == null) {
            return new LocalLockWrapper(lockKey.replace(KeyConstants.redis_lock_key, KeyConstants.local_lock_key)).tryLock(waitTime, timeUnit);
        }

        try {
            locked = rLock.tryLock(waitTime, timeout, timeUnit);
            if (locked) {
                log.debug("Redis分布式锁尝试获取成功: {}", lockKey);
            }
            return locked;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Redis分布式锁尝试获取被中断: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public void unlock() {
        if (rLock == null) {
            new LocalLockWrapper(lockKey.replace(KeyConstants.redis_lock_key, KeyConstants.local_lock_key)).unlock();
            return;
        }

        if (locked && rLock.isHeldByCurrentThread()) {
            try {
                rLock.unlock();
                locked = false;
                log.debug("Redis分布式锁释放成功: {}", lockKey);
            } catch (IllegalMonitorStateException e) {
                log.warn("Redis分布式锁释放失败，可能已超时自动释放: {}", lockKey, e);
            }
        }
    }

    @Override
    public boolean isLocked() {
        if (rLock == null) {
            return false;
        }
        return locked && rLock.isLocked();
    }
}
