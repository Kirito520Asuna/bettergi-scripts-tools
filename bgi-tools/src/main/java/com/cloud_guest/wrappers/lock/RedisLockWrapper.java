package com.cloud_guest.wrappers.lock;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.utils.LockUtil;
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
    private RLock rLock;
    private volatile boolean locked = false;

    public RedisLockWrapper(String lockKey) {
        this(lockKey, DEFAULT_TIMEOUT);
    }

    public RedisLockWrapper(String lockKey, long timeout) {
        this.lockKey = KeyConstants.redis_lock_key + lockKey;
        this.timeout = timeout;
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
            locked = rLock.tryLock(DEFAULT_WAIT_TIME, timeout, TimeUnit.MILLISECONDS);
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

        locked = rLock.tryLock();
        if (locked) {
            log.debug("Redis分布式锁尝试获取成功: {}", lockKey);
        }
        return locked;
    }

    @Override
    public boolean tryLock(long waitTime, TimeUnit timeUnit) {
        if (rLock == null) {
            return new LocalLockWrapper(lockKey.replace(KeyConstants.redis_lock_key, KeyConstants.local_lock_key)).tryLock(waitTime, timeUnit);
        }

        try {
            locked = rLock.tryLock(waitTime, timeout, TimeUnit.MILLISECONDS);
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
