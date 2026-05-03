package com.cloud_guest.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud_guest.domain.UidInfo;
import com.cloud_guest.domain.WsProxyAccess;
import com.cloud_guest.domain.auto_plan.AutoPlan;
import com.cloud_guest.mapper.BackupMapper;
import com.cloud_guest.pojo.*;
import com.cloud_guest.properties.load.LoadProperties;
import com.cloud_guest.service.*;
import com.cloud_guest.utils.IdUtils;
import com.cloud_guest.utils.StrUtils;
import com.cloud_guest.utils.yml.YmlUtils;
import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Resource
    private ApplicationService applicationService;
    String data="data";
    String config="config";
    String backup = "backup";
    @Resource
    private LoadProperties loadProperties;

    @Override
    public BackupInfo backup() {
        JSONObject jsonObject = backupV1();

        String date = DateTimeFormatter.ofPattern(DatePattern
                .PURE_DATE_PATTERN).format(LocalDateTime.now());
        String name = backup + "_" + date + "_" + IdUtils.fastUUID() + ".json";
        File backupDir = new File(backup);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        File backupFile = new File(backupDir, name);
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(jsonObject), backupFile);
        try {
            BackupInfo backupInfo = new BackupInfo();
            backupInfo.setBackupName(name);
            backupInfo.setBackupPath(backupDir.getAbsolutePath() + File.separator + name);
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
