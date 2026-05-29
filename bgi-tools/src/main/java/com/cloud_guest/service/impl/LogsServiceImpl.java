package com.cloud_guest.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloud_guest.entitys.domain.LogKey;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.entitys.pojo.DbKV;
import com.cloud_guest.service.DbKVService;
import com.cloud_guest.utils.ApplicationUtil;
import com.cloud_guest.utils.IdUtils;
import com.cloud_guest.utils.LockUtil;
import com.cloud_guest.wrappers.lock.LockWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/5/8 0:59:27
 * @Description
 */
@Slf4j
@Service
public class LogsServiceImpl implements com.cloud_guest.service.LogsService {
    @Resource
    private DbKVService dbKVService;
    @Value("${logging.file.path:./logs}")
    private String LOG_PATH;

    private String LOG_NAME;


    @PostConstruct
    public void init() {
        Environment bean = SpringUtil.getBean(Environment.class);
        String logName = bean.getProperty("logging.file.name", "bgi-tools.log");
        if (!logName.endsWith(".log")) {
            logName = logName + ".log";
        }
        // 去除文件名中的路径
        LOG_NAME = new File(logName).getName();

        log.info("日志目录: {}", LOG_PATH);
        log.info("日志文件: {}", LOG_NAME);
    }

    @Override
    public List<String> getFileNames() {
        List<String> list = new ArrayList<>();
        File logDir = new File(LOG_PATH);

        if (!logDir.exists() || !logDir.isDirectory()) {
            log.warn("日志目录不存在: {}", LOG_PATH);
        } else {
            File[] files = logDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.isDirectory()) {
                        list.add(file.getName());
                    }
                }
            }
        }
        //LOG_NAME 排第一个 其他按照时间倒序

        list.sort((a, b) -> {
            if (a.equals(LOG_NAME)) return -1;
            if (b.equals(LOG_NAME)) return 1;

            long timeA = new File(LOG_PATH, a).lastModified();
            long timeB = new File(LOG_PATH, b).lastModified();
            return Long.compare(timeB, timeA);
        });
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LogKey createLogKey() {
        String currentApplicationId = ApplicationUtil.getApplicationId();
        String token = IdUtils.getNextIdStr();
        LogKey logKey = new LogKey();
        logKey.setToken(token);
        logKey.setApplicationId(currentApplicationId);
        logKey.setCreateTime(LocalDateTime.now());
        logKey.setExpireTime(logKey.getCreateTime().plusDays(1l));
        DbKV dbKV = new DbKV();
        dbKV.setType(LogKey.class.getSimpleName());
        dbKV.setKeyName(token);
        dbKV.setValue(JSONUtil.toJsonStr(logKey));
        dbKVService.save(dbKV);
        logKey.setId(dbKV.getId().toString());
        return logKey;
    }

    @Override
    public LogKey getById(String id) {
        if (StrUtil.isBlank(id)) {
            return null;
        }

        try {
            long idL = Long.parseLong(id);
            DbKV dbKV = dbKVService.getById(idL);
            if (dbKV == null || StrUtil.isBlank(dbKV.getValue())) {
                return null;
            }

            LogKey key = JSONUtil.toBean(dbKV.getValue(), LogKey.class);
            key.setId(dbKV.getId().toString());
            return key;
        } catch (NumberFormatException e) {
            log.error("无效的 ID 格式: {}", id, e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LogKey update(LogKey logKey) {
        if (logKey == null || StrUtil.isBlank(logKey.getId())) {
            return null;
        }

        LogKey key = getById(logKey.getId());
        if (key == null) {
            throw new GlobalException("LogKey 不存在");
        }
        LockWrapper lock = LockUtil.getLock(LogKey.class.getName() + ":" + logKey.getId());
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            throw new GlobalException("存在其他操作，请稍后再试!");
        }

        try {
            //双重检测
            LogKey key1 = getById(logKey.getId());
            if (key1 == null) {
                throw new GlobalException("LogKey 不存在");
            }

            DbKV dbKV = new DbKV();
            dbKV.setId(Long.parseLong(logKey.getId()));
            dbKV.setType(LogKey.class.getSimpleName());
            dbKV.setKeyName(logKey.getToken());
            dbKV.setValue(JSONUtil.toJsonStr(logKey));
            dbKVService.updateById(dbKV);
            return logKey;
        } catch (NumberFormatException e) {
            log.error("无效的 ID 格式: {}", logKey.getId(), e);
            return null;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    @Override
    public LogKey getLogKey(String token) {
        Class<LogKey> logKeyClass = LogKey.class;
        DbKV dbKV = dbKVService.getOne(Wrappers.lambdaQuery(DbKV.class).eq(DbKV::getType, logKeyClass.getSimpleName()).eq(DbKV::getKeyName, token));
        if (dbKV == null) {
            return null;
        }
        return JSONUtil.toBean(dbKV.getValue(), logKeyClass);
    }

    @Override
    public List<LogKey> getAllExpiredLogKeyList() {
        List<LogKey> logKeys = dbKVService.list(Wrappers.lambdaQuery(DbKV.class)
                        .eq(DbKV::getType, LogKey.class.getSimpleName()))
                .stream()
                .filter(dbKV -> {
                    LogKey logKey = JSONUtil.toBean(dbKV.getValue(), LogKey.class);
                    return logKey.getExpireTime().isBefore(LocalDateTime.now());
                })
                .map(dbKV -> JSONUtil.toBean(dbKV.getValue(), LogKey.class))
                .collect(Collectors.toList());
        return logKeys;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(LogKey logKey) {
        if (logKey == null || StrUtil.isBlank(logKey.getToken())) {
            return false;
        }
        return dbKVService.remove(Wrappers.lambdaQuery(DbKV.class)
                .eq(DbKV::getKeyName, logKey.getToken())
                .eq(DbKV::getType, LogKey.class.getSimpleName()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(LogKey logKey) {
        if (logKey == null || StrUtil.isBlank(logKey.getId())) {
            return false;
        }

        DbKV dbKV = new DbKV();
        dbKV.setId(Long.parseLong(logKey.getId()));
        dbKV.setType(LogKey.class.getSimpleName());
        dbKV.setKeyName(logKey.getToken());
        dbKV.setValue(JSONUtil.toJsonStr(logKey));
        return dbKVService.saveOrUpdate(dbKV);
    }
}
