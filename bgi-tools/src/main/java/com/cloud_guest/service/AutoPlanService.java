package com.cloud_guest.service;

import com.cloud_guest.vo.AutoPlanVo;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @Author yan
 * @Date 2026/2/8 15:31:44
 * @Description
 */
public interface AutoPlanService {
    boolean delList(List<String> ids);

    boolean save(String id, String json);

    List<AutoPlanVo> find(String id);

    boolean saveDomainAll(String json);

    List<Map<String, Object>> findDomainAll();

    boolean saveCountryAll(String json);

    List<Map<String, Object>> findCountryAll();
}
