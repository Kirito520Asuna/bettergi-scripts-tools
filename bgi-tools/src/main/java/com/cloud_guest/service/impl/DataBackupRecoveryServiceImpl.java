package com.cloud_guest.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.entitys.domain.UidInfo;
import com.cloud_guest.entitys.domain.WsProxyAccess;
import com.cloud_guest.entitys.common.auto_plan.AutoPlan;
import com.cloud_guest.entitys.pojo.*;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.mapper.BackupMapper;
import com.cloud_guest.properties.load.LoadProperties;
import com.cloud_guest.service.*;
import com.cloud_guest.utils.StrUtils;
import com.cloud_guest.utils.yml.YmlUtils;
import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/3/15 22:16:42
 * @Description
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service
public class DataBackupRecoveryServiceImpl extends ServiceImpl<BackupMapper, BackupInfo> implements DataBackupRecoveryService {

    private String data = "data";
    private String config = "config";
    @Value(value = "${config.backup-path:backup}")
    private String backup;

    @Resource
    private ApplicationService applicationService;
    @Resource
    private LoadProperties loadProperties;

    @Override
    public boolean recovery(boolean isLocal, Long id, String name) {
        Map<String, Object> map = Maps.newLinkedHashMap();
        if (isLocal) {
            // 本地文件恢复逻辑
            String path = backup + File.separator + name;
            File backupDir = new File(path);
            if (!backupDir.exists()) {
                throw new GlobalException(name+"备份不存在");
            }
            String content = FileUtil.readUtf8String(path);
            JSONObject bean = JSONUtil.toBean(content, JSONObject.class);
            map.putAll( bean);
        }else {
            LambdaQueryWrapper<BackupInfo> queryWrapper = Wrappers.<BackupInfo>lambdaQuery()
                    .eq(BackupInfo::getBackupName, name)
                    .eq(BackupInfo::getId, id);
            BackupInfo info = getOne(queryWrapper);


            if (info == null) {
                throw new GlobalException("未找到备份记录: ID=" + id + ", Name=" + name);
            }

            String backupJson = info.getBackupJson();
            if (StrUtil.isBlank(backupJson)) {
                throw new GlobalException("备份数据为空: ID=" + id + ", Name=" + name);
            }


            JSONObject bean = JSONUtil.toBean(info.getBackupJson(), JSONObject.class);
            map.putAll( bean);
        }
        recovery(map);
        return true;
    }

    @Override
    public List<BackupInfo> localList() {
        File backupDir = new File(backup);
        if (!backupDir.exists()) {
            return List.of();
        }

        File[] jsonFiles = backupDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles == null || jsonFiles.length == 0) {
            return List.of();
        }

        List<BackupInfo> infos = Arrays.stream(jsonFiles)
                .map(file -> {
                    BackupInfo info = new BackupInfo();
                    info.setBackupName(file.getName());
                    info.setBackupPath(backup + File.separator + file.getName());
                    info.setBackupTime(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(file.lastModified()),
                            ZoneId.systemDefault()));
                    info.setBackupSize(file.length());

                    try {
                        String content = FileUtil.readUtf8String(file);
                        info.setBackupJson(content);
                    } catch (Exception e) {
                        log.warn("读取备份文件失败: {}", file.getName(), e);
                    }

                    return info;
                })
                .sorted((a, b) -> b.getBackupTime().compareTo(a.getBackupTime()))
                .collect(Collectors.toList());
        return infos;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatchBackup(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        List<BackupInfo> list = listByIds(ids);
        if (list.isEmpty()) {
            return true;
        }

        boolean tryLock = removeBatchByIds(ids);
        if (tryLock) {
            log.info("批量删除备份记录成功，数量: {}", list.size());
        }

        for (BackupInfo backupInfo : list) {
            String backupPath = backupInfo.getBackupPath();
            if (StrUtil.isNotBlank(backupPath)) {
                File file = new File(backupPath);
                if (file.exists()) {
                    try {
                        FileUtil.del(file);
                        log.debug("删除备份文件成功: {}", backupPath);
                    } catch (Exception e) {
                        log.error("删除备份文件失败: {}", backupPath, e);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean deleteBatchBackupLocal(List<String> paths) {
        for (String path : paths) {
            if (StrUtil.isNotBlank(path)) {
                File file = new File(path);
                if (file.exists()) {
                    try {
                        FileUtil.del(file);
                        log.debug("删除备份文件成功: {}", path);
                    } catch (Exception e) {
                        log.error("删除备份文件失败: {}", path, e);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public BackupInfo backup() {
        JSONObject jsonObject = backupV1();

        String date = DateTimeFormatter.ofPattern(DatePattern
                .PURE_DATE_PATTERN).format(LocalDateTime.now());
        String randomStr = cn.hutool.core.util.RandomUtil.randomString(6);
        String name = backup + "_" + date + "_" + randomStr + ".json";
        File backupDir = new File(backup);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        File backupFile = new File(backupDir, name);
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(jsonObject), backupFile);
        try {
            BackupInfo backupInfo = new BackupInfo();
            backupInfo.setBackupName(name);
            backupInfo.setBackupPath(backup + File.separator + name);
            backupInfo.setBackupTime(LocalDateTime.now());
            backupInfo.setBackupSize(backupFile.length());
            backupInfo.setBackupJson(JSONUtil.toJsonStr(jsonObject));

            saveOrUpdate(backupInfo);

            return backupInfo;
        } catch (Exception e) {
            FileUtil.del(backupFile);
            log.error("备份失败", e);
            throw e;
        }
        //return jsonObject;
    }

    @Override
    public boolean recovery(Map<String, Object> map) {
        Object o = map.get(data);
       if (o == null){
           log.info("旧版数据恢复");
           return recoveryLegacy( map);
       }else {
           log.info("新版数据恢复");
           return recoveryV1(map);
       }
    }

    public boolean recoveryV1(Map<String, Object> map) {
        Object o = map.get(data);
        if (o != null) {
            JSONObject bean = JSONUtil.toBean(o.toString(), JSONObject.class);
            recoveryDataV1(bean);
        }

        Object object = map.get(config);
        if (object != null) {
            JSONObject beanConfig = JSONUtil.toBean(object.toString(), JSONObject.class);
            recoveryConfigV1(beanConfig);
        }
        return true;
    }
    // 恢复处理器映射：Key = 服务后缀，Value = 恢复逻辑
    private Map<String, Consumer<String>> recoveryHandlers;
    @Resource
    private WsProxyService wsProxyService;
    @Resource
    private UidService uidService;
    @Resource
    private AutoPlanService autoPlanService;
    @Resource
    private DbKVService dbKVService;
    // 初始化恢复处理器
    @PostConstruct
    public void initRecoveryHandlers() {
        recoveryHandlers = Maps.newLinkedHashMap();

        recoveryHandlers.put(uidService.getSuffix(), json -> {
            List<?> list = JSONUtil.toList(json, uidService.getEntityClass());
            uidService.remove(Wrappers.lambdaQuery(uidService.getEntityClass())
                    .ne(UidInfoConfig::getUid, null));
            uidService.saveOrUpdateBatch((List) list);
        });

        recoveryHandlers.put(wsProxyService.getSuffix(), json -> {
            List<?> list = JSONUtil.toList(json, wsProxyService.getEntityClass());

            wsProxyService.remove(Wrappers.lambdaQuery(wsProxyService.getEntityClass())
                    .ne(WsProxyAccessConfig::getUid, null));
            wsProxyService.saveOrUpdateBatch((List) list);
        });

        recoveryHandlers.put(autoPlanService.getSuffix(), json -> {
            List<?> list = JSONUtil.toList(json, autoPlanService.getEntityClass());
            autoPlanService.remove(Wrappers.lambdaQuery(autoPlanService.getEntityClass())
                    .ne(AutoPlanConfig::getId, null));
            autoPlanService.saveOrUpdateBatch((List) list);
        });


        Consumer<String> dbConsumer = json -> {
            List<DbKV> list = JSONUtil.toList(json, dbKVService.getEntityClass())
                    .stream().map(item -> {
                        String keyName = item.getKeyName();
                        if (StrUtils.isBlank(keyName)){
                            item.setKeyName(item.getType());
                        }
                        return item;
                    })
                    .collect(Collectors.toList());
            dbKVService.remove(Wrappers.lambdaQuery(dbKVService.getEntityClass())
                    .ne(DbKV::getId, null));
            dbKVService.saveOrUpdateBatch((List) list);
        };
        recoveryHandlers.put(dbKVService.getSuffix(), dbConsumer);
    }



    public JSONObject backupV1() {
        JSONObject backup = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(uidService.getSuffix(), JSONUtil.toJsonStr(uidService.list()));
        jsonObject.put(wsProxyService.getSuffix(), JSONUtil.toJsonStr(wsProxyService.list()));
        jsonObject.put(autoPlanService.getSuffix(), JSONUtil.toJsonStr(autoPlanService.list()));
        jsonObject.put(dbKVService.getSuffix(), JSONUtil.toJsonStr(dbKVService.list()));
        backup.put(data, jsonObject);
        JSONObject jsonObjectConfig = new JSONObject();
        List<String> yamlPaths = loadProperties.getYamlPaths();
        for (String yamlPath : yamlPaths) {
            try {
                JSONObject entries = YmlUtils.readValueToJSONObject(yamlPath);
                jsonObjectConfig.putAll(entries);
            } catch (Exception e) {
                log.error("读取文件失败：{}", yamlPath, e);
            }
        }
        backup.put(config, JSONUtil.toJsonStr(jsonObjectConfig));
        return backup;
    }

    @SneakyThrows
    public boolean recoveryConfigV1(Map<String, Object> map) {
        JSONObject bean = JSONUtil.toBean(JSONUtil.toJsonStr(map), JSONObject.class);
        applicationService.saveLoadApplicationYml(bean);
        applicationService.loadApplicationYml(null);
        return true;
    }

    public boolean recoveryDataV1(Map<String, Object> map) {
        boolean allSuccess = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String json = entry.getValue().toString();
            Consumer<String> handler = recoveryHandlers.get(key);

            if (handler == null) {
                log.warn("未找到对应的恢复处理器，key: {}", key);
                continue;
            }

            try {
                handler.accept(json);
            } catch (Exception e) {
                log.error("恢复数据失败，key: {}", key, e);
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * 兼容旧版备份数据格式的导入方法
     * @param map 备份 Map，键格式如 AUTO_PLAN:UID:123、MAPPING:UID:456 等
     * @return 全部导入成功返回 true，任一失败返回 false
     */
    public boolean recoveryLegacy(Map<String, Object> map) {
        boolean allSuccess = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String valueJson = entry.getValue().toString();

            // 跳过元数据索引键（如 "AUTO_PLAN:UID"、"MAPPING:UID"）
            if (isMetaIndexKey(key)) {
                log.debug("跳过元数据索引键: {}", key);
                continue;
            }

            try {
                // 解析 data 字段
                JSONObject jsonObject = JSONUtil.parseObj(valueJson);
                String dataJson = jsonObject.getStr("data");
                if (dataJson == null) {
                    log.warn("缺少 data 字段，跳过: {}", key);
                    continue;
                }

                // 按前缀路由到具体处理器
                if (key.startsWith("AUTO_PLAN:UID:")) {
                    handleAutoPlanByUid(key, dataJson);
                } else if (key.startsWith("AUTO_PLAN_DOMAIN:ALL")) {
                    handleAutoPlanDomainConfig(dataJson);
                } else if (key.startsWith("AUTO_PLAN_COUNTRY:ALL")) {
                    handleAutoPlanCountryConfig(dataJson);
                } else if (key.startsWith("WS_PROXY_ACCESS:UID:")) {
                    handleWsProxyConfig(key, dataJson);
                } else if (key.startsWith("MAPPING:UID:")) {
                    handleMappingConfig(key, dataJson);
                } else {
                    log.warn("未知类型的键: {}", key);
                }
            } catch (Exception e) {
                log.error("恢复数据失败，key: {}", key, e);
                allSuccess = false;
            }
        }
        return allSuccess;
    }

// ---------- 以下为各处理器私有方法 ----------

    /**
     * 判断是否为元数据索引键（本身没有数据，只记录子键列表）
     */
    private boolean isMetaIndexKey(String key) {
        // 根据实际格式补充：如 "AUTO_PLAN:UID"、"MAPPING:UID" 等
        return key.matches("^[A-Z_]+:UID$");
    }

    /**
     * 处理 AUTO_PLAN:UID:xxx 类型数据
     */
    public void handleAutoPlanByUid(String key, String dataJson) {
        // 从 key 中提取 uid（例如 AUTO_PLAN:UID:125607110）
        String uid = key.substring("AUTO_PLAN:UID:".length());
        List<AutoPlanConfig> planList = JSONUtil.toList(dataJson, AutoPlan.class).stream()
                .map(AutoPlan::toConfig)
                .map(o->{
                    o.setUid(uid);
                    return o;
                })
                .toList();
        // 根据实际业务调用保存方法（此处假定有 saveByUid 方法）
        autoPlanService.saveOrUpdateBatch(planList);
        log.info("恢复自动计划成功，uid: {}", uid);
    }

    /**
     * 处理 AUTO_PLAN_DOMAIN:ALL 全局域名配置
     */
    public void handleAutoPlanDomainConfig(String dataJson) {
        //List<String> domains = JSONUtil.toList(dataJson, String.class);
        autoPlanService.saveDomainAll(dataJson);
        log.info("恢复域名配置成功");
    }

    /**
     * 处理 AUTO_PLAN_COUNTRY:ALL 全局国家配置
     */
    public void handleAutoPlanCountryConfig(String dataJson) {
        //List<String> countries = JSONUtil.toList(dataJson, String.class);
        autoPlanService.saveCountryAll(dataJson);
        log.info("恢复国家配置成功");
    }

    /**
     * 处理 WS_PROXY_ACCESS:UID:xxx 数据
     */
    public void handleWsProxyConfig(String key, String dataJson) {
        String uid = key.substring("WS_PROXY_ACCESS:UID:".length());
        WsProxyAccessConfig access = JSONUtil.toBean(dataJson, WsProxyAccess.class).toConfig();
        wsProxyService.saveOrUpdate(access);
        log.info("恢复 WS 代理配置成功，uid: {}", uid);
    }

    /**
     * 处理 MAPPING:UID:xxx 数据（用户映射）
     */
    public void handleMappingConfig(String key, String dataJson) {
        String uid = key.substring("MAPPING:UID:".length());
        // 若已有 MappingService 则替换为对应服务
        UidInfoConfig mapping = JSONUtil.toBean(dataJson, UidInfo.class).toConfig();
        uidService.saveOrUpdate(mapping); // 假设 uidService 支持该操作
        log.info("恢复用户映射成功，uid: {}", uid);
    }
}
