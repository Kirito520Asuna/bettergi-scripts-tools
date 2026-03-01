package com.cloud_guest.utils;

import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.utils.yml.YmlUtils;
import com.cloud_guest.wrappers.lock.LockWrapper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2026/2/28 22:15:33
 * @Description
 */
public class LockYmlUtil extends YmlUtils {
    /**
     * 向指定路径写入值的方法重载，使用锁键获取锁
     * @param path 要写入的文件路径
     * @param value 要写入的值对象
     * @param lockKey 用于获取锁的键
     * @throws IOException 如果发生I/O错误
     */
    public static void writeValue(Path path, Object value, String lockKey) throws IOException {
        // 通过锁键获取锁对象
        LockWrapper lock = LockUtil.getLock(lockKey);
        // 调用另一个重载方法，传入获取的锁对象
        writeValue(path, value, lock);
    }

    /**
     * 将指定值写入到指定路径的文件中，并在操作期间使用锁机制确保线程安全
     *
     * @param path 目标文件的路径
     * @param value 要写入的值
     * @param lock 用于线程同步的锁对象
     * @throws IOException 如果发生I/O错误
     * @throws GlobalException 如果无法获取锁，表示存在其他操作正在进行
     */
    public static void writeValue(Path path, Object value, LockWrapper lock) throws IOException {
        // 尝试获取锁
        boolean tryLock = lock.tryLock();
        // 如果获取锁失败，抛出异常提示用户稍后重试
        if (!tryLock) {
            throw new GlobalException("存在其他操作，请稍后再试!");
        }
        try {
            // 使用YmlUtils工具类将值写入文件
            YmlUtils.writeValue(path.toFile(), value);
        } finally {
            // 确保在方法返回前释放锁（如果已被锁定）
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }
    public static void writeValue(File file, Object value, LockWrapper lock) throws IOException {
        // 尝试获取锁
        boolean tryLock = lock.tryLock();
        // 如果获取锁失败，抛出异常提示用户稍后重试
        if (!tryLock) {
            throw new GlobalException("存在其他操作，请稍后再试!");
        }
        try {
            // 使用YmlUtils工具类将值写入文件
            YmlUtils.writeValue(file, value);
        } finally {
            // 确保在方法返回前释放锁（如果已被锁定）
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }
    /**
     * 向指定路径写入值的方法，使用指定的锁键和超时时间获取锁
     * @param path 要写入值的文件路径
     * @param value 要写入的值对象
     * @param lockKey 用于获取锁的键
     * @param timeout 获取锁的超时时间
     * @param timeUnit 超时时间单位
     * @throws IOException 如果发生I/O错误
     */
    public static void writeValue(Path path, Object value, String lockKey, long timeout, TimeUnit timeUnit) throws IOException {
        // 调用重载的writeValue方法，传入通过LockUtil获取的锁
        writeValue(path, value, LockUtil.getLock(lockKey, timeout, timeUnit));
    }


    /**
     * 向输出流写入值的方法
     * @param out 输出流，用于写入数据
     * @param value 需要写入的值，可以是任意Object类型
     * @param lockKey 锁的键值，用于标识特定的锁
     * @param timeout 获取锁的超时时间
     * @param timeUnit 超时时间的时间单位
     * @throws IOException 如果发生I/O错误
     */
    public static void writeValue(OutputStream out, Object value, String lockKey, long timeout, TimeUnit timeUnit) throws IOException {
        // 调用重载的writeValue方法，先通过LockUtil获取锁，然后执行写入操作
        writeValue(out, value, LockUtil.getLock(lockKey, timeout, timeUnit));
    }

    /**
     * 将指定值写入输出流，确保线程安全操作
     * @param out 目标输出流
     * @param value 要写入的值
     * @param lock 用于保证线程安全的锁包装器
     * @throws IOException 发生I/O错误时抛出
     * @throws GlobalException 当无法获取锁时抛出，表示存在其他操作正在进行
     */
    public static void writeValue(OutputStream out, Object value, LockWrapper lock) throws IOException {
        // 尝试获取锁
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            // 如果获取锁失败，抛出异常提示用户稍后重试
            throw new GlobalException("存在其他操作，请稍后再试!");
        }
        try {
            // 使用YmlUtils将值写入输出流
            YmlUtils.writeValue(out, value);
        } finally {
            // 确保在操作完成后释放锁
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }
}
