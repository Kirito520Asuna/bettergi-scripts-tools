package com.cloud_guest.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.wrappers.lock.LocalLockWrapper;
import com.cloud_guest.wrappers.lock.LockWrapper;
import com.cloud_guest.wrappers.lock.RedisLockWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author yan
 * @Date 2026/2/28 21:14:23
 * @Description 锁工具类，支持本地锁和Redis分布式锁
 */
@Slf4j
public class LockUtil {

    // 本地锁容器
    private static final ConcurrentHashMap<String, ReentrantLock> LOCAL_LOCKS = new ConcurrentHashMap<>();

    // 锁的默认超时时间（毫秒）
    private static final long DEFAULT_TIMEOUT = 10000L;

    // 锁的默认等待时间（毫秒）
    private static final long DEFAULT_WAIT_TIME = 5000L;

    /**
     * 获取锁 - 根据配置自动选择本地锁或Redis锁
     *
     * @param lockKey 锁的key
     * @return LockWrapper 锁包装器
     */
    public static LockWrapper getLock(String lockKey) {
        if (StrUtil.isBlank(lockKey)) {
            throw new IllegalArgumentException("锁key不能为空");
        }

        if (ModeUtil.isLocal()) {
            return new LocalLockWrapper(lockKey);
        } else if (ModeUtil.isRedis()) {
            return new RedisLockWrapper(lockKey);
        } else {
            throw new IllegalStateException("未识别的锁模式");
        }
    }

    /**
     * 获取锁 - 指定超时时间
     *
     * @param lockKey 锁的key
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @return LockWrapper 锁包装器
     */
    public static LockWrapper getLock(String lockKey, long timeout, TimeUnit timeUnit) {
        if (StrUtil.isBlank(lockKey)) {
            throw new IllegalArgumentException("锁key不能为空");
        }

        long timeoutMillis = timeUnit.toMillis(timeout);
        if (ModeUtil.isLocal()) {
            return new LocalLockWrapper(lockKey, timeoutMillis);
        } else if (ModeUtil.isRedis()) {
            return new RedisLockWrapper(lockKey, timeoutMillis);
        } else {
            throw new IllegalStateException("未识别的锁模式");
        }
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的key
     * @return boolean 是否获取成功
     */
    public static boolean tryLock(String lockKey) {
        return getLock(lockKey).tryLock();
    }

    /**
     * 尝试获取锁 - 指定等待时间
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param timeUnit 时间单位
     * @return boolean 是否获取成功
     */
    public static boolean tryLock(String lockKey, long waitTime, TimeUnit timeUnit) {
        return getLock(lockKey).tryLock(waitTime, timeUnit);
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的key
     */
    public static void unlock(String lockKey) {
        if (StrUtil.isBlank(lockKey)) {
            return;
        }
        LockWrapper localLock;
        if (ModeUtil.isLocal()) {
            localLock = new LocalLockWrapper(lockKey);
        } else if (ModeUtil.isRedis()) {
            localLock = new RedisLockWrapper(lockKey);
        }else {
            throw new IllegalStateException("未识别的锁模式");
        }
        localLock.unlock();
    }



    public static void main(String[] args) {
        // 示例使用
        String lockKey = "test-lock-key";

        if (ModeUtil.isLocal()) {
            // 本地锁示例
            LockWrapper localLock = LockUtil.getLock(lockKey);
            if (localLock.tryLock()) {
                try {
                    System.out.println("本地锁获取成功，执行业务逻辑...");
                    // 执行业务逻辑
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    localLock.unlock();
                    System.out.println("本地锁释放完成");
                }
            } else {
                System.out.println("本地锁获取失败");
            }
        } else if (ModeUtil.isRedis()) {
            // Redis锁示例
            LockWrapper redisLock = LockUtil.getLock(lockKey, 30, TimeUnit.SECONDS);
            if (redisLock.lock()) {
                try {
                    System.out.println("Redis分布式锁获取成功，执行业务逻辑...");
                    // 执行业务逻辑
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    redisLock.unlock();
                    System.out.println("Redis分布式锁释放完成");
                }
            } else {
                System.out.println("Redis分布式锁获取失败");
            }
        }
    }
}

