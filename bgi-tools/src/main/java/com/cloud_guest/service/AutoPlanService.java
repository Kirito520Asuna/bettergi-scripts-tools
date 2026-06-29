package com.cloud_guest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.entitys.pojo.AutoPlanConfig;

import java.util.List;
import java.util.Map;

/**
 * @Author yan
 * @Date 2026/2/8 15:31:44
 * @Description
 */
public interface AutoPlanService extends IService<AutoPlanConfig>, BaseService {
    default String getSuffix() {
        return KeyConstants.auto_plan_key;
    }
    boolean removeByUidList(List<String> uidList);
    //boolean delList(List<String> ids);

    //boolean save(String id, String json);
    //@Deprecated
    //List<String> findALLUid();

    //List<AutoPlanVo> find(String id);
    List<AutoPlanConfig> find(String uid,Boolean enable);

    boolean saveDomainAll(String json);
    boolean saveBossAll(String json);

    List<String> findUidAll();

    //boolean saveUid(String uid);
    List<Map<String, Object>> findBossAll();

    List<Map<String, Object>> findDomainAll();

    boolean saveCountryAll(String json);

    List<String> findCountryAll();

    boolean saveOrUpdateBatchList(List<AutoPlanConfig> configList, Boolean removeCultivate);

    boolean saveDomainAllByAdd( String json);
    boolean saveBossAllByAdd( String json);

    boolean saveCountryAllByAdd(String json);
}
