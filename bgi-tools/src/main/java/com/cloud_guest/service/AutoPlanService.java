package com.cloud_guest.service;

import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.vo.AutoPlanVo;

import java.util.List;
import java.util.Map;

/**
 * @Author yan
 * @Date 2026/2/8 15:31:44
 * @Description
 */
public interface AutoPlanService extends BaseService {
    @Override
    default String getSuffix() {
        return KeyConstants.auto_plan_key;
    }

    boolean delList(List<String> ids);

    boolean save(String id, String json);
    @Deprecated
    List<String> findALLUid();

    List<AutoPlanVo> find(String id);

    boolean saveDomainAll(String json);

    List<String> findUidAll();

    boolean saveUid(String uid);

    List<Map<String, Object>> findDomainAll();

    boolean saveCountryAll(String json);

    List<String> findCountryAll();
}
