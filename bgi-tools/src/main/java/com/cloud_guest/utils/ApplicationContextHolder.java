package com.cloud_guest.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.BgiToolsApplication;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.ApplicationInfo;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.redis.service.RedisService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.object.ObjectUtils;
import com.cloud_guest.wrappers.lock.LockWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/27 21:49:23
 * @Description
 */
// 1. 先创建一个工具类保存上下文和启动参数
@Component
@Slf4j
public class ApplicationContextHolder {

    private static ConfigurableApplicationContext context;
    private static String[] args;
    private static String restartKey = KeyConstants.all_application_key + ":" + KeyConstants.restart_key;
    private static long reportedOnlineTimeout = 1 * 60 * 1000;// 1分钟
    private static boolean taskSettings = false;// 1分钟

    public static void setTaskSettings(boolean settings) {
        taskSettings = settings;
    }

    public static boolean getTaskSettings() {
        return taskSettings;
    }

    public static void setReportedOnlineTimeout(long timeout) {
        reportedOnlineTimeout = timeout;
    }

    public static long getReportedOnlineTimeout() {
        return reportedOnlineTimeout;
    }

    public static void setContext(ConfigurableApplicationContext ctx, String[] startupArgs) {
        context = ctx;
        args = startupArgs;
    }

    public static void restart() {
        Thread restartThread = new Thread(() -> {
            try {
                LockWrapper lock = LockUtil.getLock(restartKey);
                boolean tryLock = lock.tryLock();
                if (!tryLock) {
                    throw new GlobalException("获取锁失败:" + restartKey);
                }
                try {
                    ApplicationInfo applicationInfo = ApplicationUtil.getApplicationInfo();
                    //设置重启key
                    SpringUtil.getBean(CacheService.class).saveId(restartKey, JSONUtil.toJsonStr(applicationInfo.toReportedOnline()));
                } finally {
                    if (tryLock) {
                        lock.unlock();
                    }
                }

                String active = SpringUtil.getBean(Environment.class).getProperty("spring.profiles.active", String.class);
                Thread.sleep(1000);  // 可选：缓冲时间

                // 1. 关闭旧上下文（触发优雅关闭）
                if (context != null && context.isRunning()) {
                    context.close();
                }

                // 2. 显式创建 SpringApplication 并强制设为 SERVLET 类型
                SpringApplication application = new SpringApplication(BgiToolsApplication.class);  // YourApplication 换成你的 @SpringBootApplication 类
                application.setWebApplicationType(WebApplicationType.SERVLET);  // ← 关键这一行！

                // 可选：如果有其他配置（如 profiles、banner 等），在这里设置
                application.setAdditionalProfiles(active);
                // application.setBannerMode(Banner.Mode.OFF);

                // 3. 重新 run
                ConfigurableApplicationContext newContext = application.run(args);

                // 更新 holder
                setContext(newContext, args);

                log.info("应用已成功重启");

            } catch (Exception e) {
                log.error("重启失败: " + e.getMessage());
                e.printStackTrace();
            }
        });

        restartThread.setDaemon(false);
        restartThread.start();
    }

    /**
     * 保存上报的在线信息
     *
     * @param key             缓存键
     * @param applicationInfo 应用信息对象
     * @return 保存结果，成功返回true
     */
    private static boolean saveReportedOnline(String key, ApplicationInfo applicationInfo, Boolean add) {
        if (ObjectUtils.isEmpty(add)) {
            add = false;
        }
        // 获取CacheService实例
        CacheService bean = SpringUtil.getBean(CacheService.class);

        // 定义锁键并获取锁对象
        String lockKey = key;
        LockWrapper lock = LockUtil.getLock(lockKey);
        // 尝试获取锁
        boolean tryLock = lock.tryLock();
        // 获取锁失败，抛出异常
        if (!tryLock) {
            throw new GlobalException("存在其他操作，请稍后再试!");
        }
        // 创建LinkedHashSet集合用于存储应用信息

        // 根据键从缓存中获取值
        Set<ApplicationInfo> hashSet = new LinkedHashSet<>();
        String values = bean.findValueByKey(key);
        if (StrUtil.isNotBlank(values)) {
            if (JSONUtil.isTypeJSONArray(values)) {
                // 是数组
                JSONUtil.toList(values, ApplicationInfo.class).forEach(hashSet::add);
            } else {
                // 不是数组
                hashSet.add(JSONUtil.toBean(values, ApplicationInfo.class));
            }
        }

        String applicationId = applicationInfo.getApplicationId();
        Long datacenterId = applicationInfo.getDatacenterId();
        hashSet.removeIf(a -> ObjectUtils.equals(a.getApplicationId(), applicationId)
                && ObjectUtils.equals(a.getDatacenterId(), datacenterId));
        if (add) {
            hashSet.add(applicationInfo);
        }

        try {
            String jsonStr = JSONUtil.toJsonStr(hashSet.stream().collect(Collectors.toList()));
            if (ModeUtil.isLocal()) {
                LocalCacheUtils.put(key, jsonStr);
            } else if (ModeUtil.isRedis()) {
                String keyRedis = KeyConstants.redis_file_json_key + key;
                RedisService redisService = SpringUtil.getBean(RedisService.class);
                redisService.save(keyRedis, jsonStr);
            }
        } finally {
            if (tryLock) {
                lock.unlock();
            }
        }
        return true;
    }

    /**
     * 上报应用在线信息
     *
     * @param applicationInfo
     */
    public static void reportedOnline(ApplicationInfo applicationInfo) {
        String onlineApplicationKey = KeyConstants.online_application_key;
        saveReportedOnline(onlineApplicationKey, applicationInfo, true);
    }

    /**
     * 移除上报
     *
     * @param applicationInfo
     */
    public static void clearReportedOnline(ApplicationInfo applicationInfo) {
        saveReportedOnline(KeyConstants.online_application_key, applicationInfo, false);
    }

    /**
     * 检查在线并获取在线
     *
     * @param reportedOnlineTimeout 上线报备超时时间，单位为毫秒
     * @return
     */
    public static List<ApplicationInfo> checkAndGetOnline(Long reportedOnlineTimeout) {
        if (reportedOnlineTimeout == null) {
            reportedOnlineTimeout = getReportedOnlineTimeout();
        }
        // 从Spring容器中获取CacheService的Bean实例
        CacheService bean = SpringUtil.getBean(CacheService.class);
        // 定义用于存储上线应用信息的缓存键
        String outlineApplicationKey = KeyConstants.outline_application_key;
        // 从缓存中获取键对应的值
        String onlineApplicationKey = KeyConstants.online_application_key;
        String valueByKey = bean.findValueByKey(onlineApplicationKey);
        if (StrUtil.isBlank(valueByKey)) {
            return new ArrayList<>();
        }
        // 获取指定键的锁对象
        LockWrapper lock = LockUtil.getLock(outlineApplicationKey);
        boolean tryLock = lock.isLocked();
        // 尝试获取锁，最多等待1分钟
        tryLock = lock.tryLock(800l, TimeUnit.MILLISECONDS);
        // 如果获取锁失败，抛出全局异常
        if (!tryLock) {
            throw new GlobalException("获取锁失败:" + outlineApplicationKey);
        }

        try {
            // 从缓存中获取键对应的值
             valueByKey = bean.findValueByKey(onlineApplicationKey);
            // 创建用于存储检查在线键的列表
            List<String> checkOnlineKeys = new ArrayList<>();  // 修复：使用可变列表

            // 如果获取到的值不为空
            if (StrUtil.isNotBlank(valueByKey)) {
                // 判断值是否为JSONArray类型
                if (JSONUtil.isTypeJSONArray(valueByKey)) {
                    // 如果是JSONArray类型，将所有元素添加到列表中
                    checkOnlineKeys.addAll(JSONUtil.toList(valueByKey, String.class));
                } else {
                    // 如果不是JSONArray类型，直接将值添加到列表中
                    checkOnlineKeys.add(valueByKey);
                }
            }

            // 将JSON字符串转换为ApplicationInfo对象列表
            List<ApplicationInfo> checkOnlineList = checkOnlineKeys.stream().map(jsonStr -> JSONUtil.toBean(jsonStr, ApplicationInfo.class))
                    .collect(Collectors.toList());
            List<ApplicationInfo> outList = new ArrayList<>();
            List<ApplicationInfo> onlineList = new ArrayList<>();
            long currentTimeMillis = System.currentTimeMillis();

            for (ApplicationInfo applicationInfo : checkOnlineList) {
                Long timeStamp = applicationInfo.getTimeStamp();
                long timeout = currentTimeMillis - timeStamp;
                log.debug("applicationInfo:{},timeout:{},reportedOnlineTimeout:{}", applicationInfo, timeout, reportedOnlineTimeout);
                if (ObjectUtils.isNotEmpty(applicationInfo.getTimeout()) && timeout > applicationInfo.getTimeout()) {
                    //标记需要处理
                    outList.add(applicationInfo);
                } else if (timeout > reportedOnlineTimeout) {
                    //标记需要处理
                    outList.add(applicationInfo);
                } else {
                    onlineList.add(applicationInfo);
                }
            }

            //ThreadPoolTaskExecutor executor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);
            //CompletableFuture.runAsync(() -> {
            log.debug("在线检查完成 - 正常在线：{}, 超时离线：{}",
                    onlineList.size(), outList.size());
            // 异步处理超时离线
            for (ApplicationInfo applicationInfo : outList) {
                applicationInfo.setTimeStamp(null);
                bean.saveId(outlineApplicationKey, JSONUtil.toJsonStr(applicationInfo));
            }
            //}, executor);

            return onlineList;
        } finally {
            if (tryLock) {
                lock.unlock();
            }
        }

    }

    /**
     * 清除离线键
     */
    public static void clearOutlineKeys() {
        CacheService bean = SpringUtil.getBean(CacheService.class);
        String outlineApplicationKey = KeyConstants.outline_application_key;
        // 第一次检查：不加锁检查是否有值
        String valueByKey = bean.findValueByKey(outlineApplicationKey);
        if (StrUtil.isBlank(valueByKey)) {
            return;
        }
        LockWrapper lock = LockUtil.getLock(outlineApplicationKey);
        boolean tryLock = lock.tryLock(800l, TimeUnit.MILLISECONDS);
        if (!tryLock) {
            throw new GlobalException("获取锁失败:" + outlineApplicationKey);
        }
        try {
            valueByKey = bean.findValueByKey(outlineApplicationKey);
            // 创建一个可变列表用于存储重启键
            List<String> keys = new ArrayList<>();  // 修复：使用可变列表

            // 如果获取到的值不为空
            if (StrUtil.isNotBlank(valueByKey)) {
                // 判断值是否为JSONArray类型
                if (JSONUtil.isTypeJSONArray(valueByKey)) {
                    // 如果是JSONArray类型，将所有元素添加到列表中
                    keys.addAll(JSONUtil.toList(valueByKey, String.class));
                } else {
                    // 如果不是JSONArray类型，直接将值添加到列表中
                    keys.add(valueByKey);
                }
            }
            List<ApplicationInfo> outlineList = keys.stream().map(jsonStr -> JSONUtil.toBean(jsonStr, ApplicationInfo.class)).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(outlineList)) {
                log.warn("发现 {} 个离线的应用实例需要清理", outlineList.size());
                for (ApplicationInfo applicationInfo : outlineList) {
                    log.warn("清理离线应用实例：{}", applicationInfo);
                    clearReportedOnline(applicationInfo);
                }
                bean.removeByKey(outlineApplicationKey);
            }
        } finally {
            if (tryLock) {
                lock.unlock();
            }
        }
    }


    /**
     * 清理重启键
     */
    public static void clearRestartKeys() {
        try {
            // 从Spring容器中获取CacheService的Bean实例
            CacheService bean = SpringUtil.getBean(CacheService.class);

            // 第一次检查：不加锁检查是否有值
            String valueByKey = bean.findValueByKey(restartKey);
            if (StrUtil.isBlank(valueByKey)) {
                return;
            }

            LockWrapper lock = LockUtil.getLock(restartKey);
            boolean tryLock = lock.tryLock(800l, TimeUnit.MILLISECONDS);
            if (!tryLock) {
                throw new GlobalException("获取锁失败:" + restartKey);
            }
            try {
                // 根据重启键的键名获取存储的值
                valueByKey = bean.findValueByKey(restartKey);
                // 创建一个可变列表用于存储重启键
                List<String> restartKeys = new ArrayList<>();  // 修复：使用可变列表

                // 如果获取到的值不为空
                if (StrUtil.isNotBlank(valueByKey)) {
                    // 判断值是否为JSONArray类型
                    if (JSONUtil.isTypeJSONArray(valueByKey)) {
                        // 如果是JSONArray类型，将所有元素添加到列表中
                        restartKeys.addAll(JSONUtil.toList(valueByKey, String.class));
                    } else {
                        // 如果不是JSONArray类型，直接将值添加到列表中
                        restartKeys.add(valueByKey);
                    }
                }
                // 获取所有当前运行的应用ID列表
                List<String> applicationIds = ApplicationUtil.getAllApplicationIds();
                // 筛选出需要清理的异常重启键
                List<ApplicationInfo> staleKeys = restartKeys.stream()
                        .map(jsonStr -> JSONUtil.toBean(jsonStr, ApplicationInfo.class))
                        //重启后会重新生成
                        .filter(key -> !applicationIds.contains(key.getApplicationId()))
                        .collect(Collectors.toList());

                // 如果存在需要清理的异常重启键
                if (CollUtil.isNotEmpty(staleKeys)) {
                    // 记录需要清理的异常重启键数量
                    log.warn("发现 {} 个重启下线的应用实例需要清理", staleKeys.size());
                    // 遍历并清理每个异常重启键
                    for (ApplicationInfo key : staleKeys) {
                        try {
                            // 记录清理信息
                            log.debug("清理重启应用实例 - ApplicationInfo:{}", key);
                            clearReportedOnline(key);
                        } catch (Exception e) {
                            log.error("清理重启键失败: {}", key, e);
                        }
                    }
                    // 清理重启键
                    bean.removeByKey(restartKey);
                }
            } finally {
                if (tryLock) {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("清理重启键过程出现异常", e);
        }
    }

}